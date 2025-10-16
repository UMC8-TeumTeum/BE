package umc.teumteum.server.domain.home.service;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import umc.teumteum.server.domain.home.converter.ScheduleConverter;
import umc.teumteum.server.domain.home.converter.WishConverter;
import umc.teumteum.server.domain.home.dto.request.*;
import umc.teumteum.server.domain.home.dto.response.*;
import umc.teumteum.server.domain.home.entity.Category;
import umc.teumteum.server.domain.home.entity.Schedule;
import umc.teumteum.server.domain.home.entity.ScheduleReminder;
import umc.teumteum.server.domain.home.entity.Wish;
import umc.teumteum.server.domain.home.entity.enums.*;
import umc.teumteum.server.domain.home.entity.mapping.WishCategory;
import umc.teumteum.server.domain.home.exception.status.HomeErrorStatus;
import umc.teumteum.server.domain.home.exception.HomeException;
import umc.teumteum.server.domain.home.repository.*;
import umc.teumteum.server.domain.teum.entity.TeumRequest;
import umc.teumteum.server.domain.teum.entity.enums.ResponseStatus;
import umc.teumteum.server.domain.teum.exception.status.TeumErrorStatus;
import umc.teumteum.server.domain.teum.repository.TeumRequestRepository;
import umc.teumteum.server.domain.user.entity.RemindAlarm;
import umc.teumteum.server.domain.user.entity.Routine;
import umc.teumteum.server.domain.user.entity.User;
import umc.teumteum.server.domain.user.entity.enums.Weekday;
import umc.teumteum.server.domain.user.repository.RemindAlarmRepository;
import umc.teumteum.server.domain.user.repository.RoutineRepository;
import umc.teumteum.server.domain.user.repository.UserRepository;
import umc.teumteum.server.global.exception.GeneralException;
import umc.teumteum.server.global.util.S3Util;
import umc.teumteum.server.global.validator.ConflictValidator;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class HomeServiceImpl implements HomeService {

    private final ScheduleConverter scheduleConverter;
    private final WishConverter wishConverter;
    private final ScheduleRepository scheduleRepository;
    private final ScheduleReminderRepository scheduleReminderRepository;
    private final TeumRequestRepository teumRequestRepository;
    private final UserRepository userRepository;
    private final WishRepository wishRepository;
    private final CategoryRepository categoryRepository;
    private final S3Util s3Util;
    private final RoutineRepository routineRepository;
    private final ConflictValidator conflictValidator;
    private final RemindAlarmRepository remindAlarmRepository;

    @Transactional
    @Override
    public HomeResponseDto.TodoIdDto createTodo(HomeRequestDto.TodoRequestDto dto, User user) {
        // Todo 등록

        // 시간 유효성 검사
        if(dto.getEndTime().isBefore(dto.getStartTime()) || dto.getEndTime().isEqual(dto.getStartTime())){
            throw new HomeException(HomeErrorStatus._INVALID_TIME_RANGE);
        }

        // 충돌 검사
        conflictValidator.validateTodo(user,dto.getStartTime(), dto.getEndTime());

        // 1. 스케줄 저장
        Schedule schedule = scheduleConverter.toSchedule(dto,user);
        Schedule savedSchedule = scheduleRepository.save(schedule);

        // 2. 스케줄 리마인드 알림 저장
        if (dto.getRemindAlarm() != null && !dto.getRemindAlarm().isEmpty()) {
            List<ScheduleReminder> reminders =
                    scheduleConverter.toScheduleReminders(savedSchedule, dto.getRemindAlarm());
            scheduleReminderRepository.saveAll(reminders);
        }

        return new HomeResponseDto.TodoIdDto(savedSchedule.getId());
    }

    @Override
    public HomeResponseDto.TodoInfoDto getTodoInfo(Long scheduleId) {
        // Todo(Schedule) 조회
        // 가상의 루틴 ID일 경우
        if(scheduleId<0){
            // ID 파싱
            HomeResponseDto.VirtualRoutineDto info = getVirtualRoutine(scheduleId);
            LocalDate date = info.getDate();
            Long routineId = info.getRoutineId();

            Routine routine = routineRepository.findById(routineId)
                    .orElseThrow(() -> new HomeException(HomeErrorStatus._ROUTINE_NOT_FOUND));

            // 프로필 조회
            String profileImageName = routine.getUser().getProfileImageName();
            List<String> profileUrls = profileImageName != null ? List.of(s3Util.toPresignedUrl("profile/" + profileImageName, Duration.ofMinutes(30))) : List.of();

            // 온보딩 리마인드 알림 조회
            List<RemindAlarm> onboardingReminders = remindAlarmRepository.findAllByUser(routine.getUser());

            return scheduleConverter.toVirtualRoutineInfo(routine,date,onboardingReminders,profileUrls);
        }

        Schedule schedule = scheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new HomeException(HomeErrorStatus._SCHEDULE_NOT_FOUND));

        // 라미인드 알림 조회
        List<ScheduleReminder> reminders = scheduleReminderRepository.findByScheduleId(schedule.getId());

        // 프로필 조회
        List<String> profileUrls;
        if(schedule.getType() == ScheduleType.TEUM){ // TEUM 타입인 경우
            profileUrls = getTeumProfileUrls(schedule);
        } else{
            String profileImageName = schedule.getUser().getProfileImageName();
            profileUrls = profileImageName != null ? List.of(s3Util.toPresignedUrl("profile/" + profileImageName, Duration.ofMinutes(30))) : List.of();
        }

        return scheduleConverter.toTodoInfoResponse(schedule, reminders, profileUrls);
    }

    private List<String> getTeumProfileUrls(Schedule schedule){
        // TEUM 타입 프로필 조회
        Long teumRequestId = schedule.getTeumRequest().getId();

        // 1. 틈 요청한 사람 ID 조회
        TeumRequest teumRequest = teumRequestRepository.findById(teumRequestId)
                .orElseThrow(()-> new GeneralException(TeumErrorStatus.TEUM_REQUEST_NOT_FOUND));

        Long requestUserId =  teumRequest.getUser().getId();

        // 2. 수락한 응답자 ID
        List<Long> acceptedUserIds = teumRequest.getTeumResponses().stream()
                .filter(r -> r.getStatus() == ResponseStatus.ACCEPTED)
                .map(r -> r.getReceiverUser().getId())
                .toList();

        // 두 개 합쳐서 반환
        List<Long> userIds = new ArrayList<>(acceptedUserIds);
        userIds.add(requestUserId);

        return userRepository.findAllById(userIds).stream()
                .map(User::getProfileImageName)
                .filter(Objects::nonNull)
                .map(name -> s3Util.toPresignedUrl("profile/" + name, Duration.ofMinutes(30)))
                .toList();
    }

    @Transactional
    @Override
    public HomeResponseDto.TodoIdDto updateTodoInfo(HomeRequestDto.TodoRequestDto dto, Long scheduleId, User user) {
        // Todo(Schedule) 수정
        if (scheduleId < 0) {
            // 1. 미래의 반복일정 수정
            return updateFutureRoutine(dto, scheduleId, user);
        }

        // 2. 스케줄 테이블 조회
        Schedule schedule = scheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new HomeException(HomeErrorStatus._SCHEDULE_NOT_FOUND));

        if (schedule.getType() == ScheduleType.ROUTINE) {
            // 3. 현재, 과거의 반복일정 수정
            return updateCurrentRoutine(dto,schedule);
        }

        // 4. 시간 유효성 검사
        if(dto.getEndTime().isBefore(dto.getStartTime()) || dto.getEndTime().isEqual(dto.getStartTime())){
            throw new HomeException(HomeErrorStatus._INVALID_TIME_RANGE);
        }

        // 5. 충돌 검사
        conflictValidator.validateTodo(user,dto.getStartTime(), dto.getEndTime());

        // 6. 스케줄 필드 업데이트
        schedule.updateField(dto);

        // 7. 스케줄 리마인드 알림 제거 -> 새로운 리마인드 알림 저장
        // 7-1. 기존 리마인드 알림 삭제
        scheduleReminderRepository.deleteByScheduleId(scheduleId);

        // 7-2. 리마인드 알림 필드 업데이트
        if (dto.getRemindAlarm() != null && !dto.getRemindAlarm().isEmpty()) {
            List<ScheduleReminder> reminders =
                    scheduleConverter.toScheduleReminders(schedule, dto.getRemindAlarm());
            scheduleReminderRepository.saveAll(reminders);
        }
        return new HomeResponseDto.TodoIdDto(schedule.getId());
    }

    @Transactional
    public HomeResponseDto.TodoIdDto updateFutureRoutine(HomeRequestDto.TodoRequestDto dto, Long scheduleId, User user) {
        // 미래의 반복일정 수정로직

        // 1. 루틴 파싱
        HomeResponseDto.VirtualRoutineDto parsedRoutine =  getVirtualRoutine(scheduleId);
        LocalDate date = parsedRoutine.getDate();
        Long  routineId = parsedRoutine.getRoutineId();

        Routine routine = routineRepository.findById(routineId)
                .orElseThrow(() -> new HomeException(HomeErrorStatus._ROUTINE_NOT_FOUND));

        // 1. 필드 수정 여부 판단
        LocalDateTime start = dto.getStartTime();
        LocalDateTime end = dto.getEndTime();

        // 2-1. 날짜 수정 여부
        if(!start.toLocalDate().equals(date) || !end.toLocalDate().equals(date)){
            throw new HomeException(HomeErrorStatus._ROUTINE_FIELDS_IMMUTABLE);
        }

        // 2-2. 기타 필드 수정 여부
        if(dto.getTitle() != null && !dto.getTitle().equals(routine.getTitle())) {
            throw new HomeException(HomeErrorStatus._ROUTINE_FIELDS_IMMUTABLE);
        }
        if (dto.getDescription() != null && !dto.getDescription().equals(routine.getDescription())) {
            throw new HomeException(HomeErrorStatus._ROUTINE_FIELDS_IMMUTABLE);
        }
        if (dto.getIsPublic() != null && !dto.getIsPublic().equals(false)) {
            throw new HomeException(HomeErrorStatus._ROUTINE_FIELDS_IMMUTABLE);
        }
        if (dto.getIncludeTeum() != null && !dto.getIncludeTeum().equals(false)) {
            throw new HomeException(HomeErrorStatus._ROUTINE_FIELDS_IMMUTABLE);
        }
        if (dto.getStartTime().toLocalTime() != null && !dto.getStartTime().toLocalTime().equals(routine.getStartTime())) {
            throw new HomeException(HomeErrorStatus._ROUTINE_FIELDS_IMMUTABLE);
        }
        if (dto.getStartTime().toLocalTime() != null && !dto.getEndTime().toLocalTime().equals(routine.getEndTime())) {
            throw new HomeException(HomeErrorStatus._ROUTINE_FIELDS_IMMUTABLE);
        }

        // 3. 스케줄 테이블에 저장
        Schedule schedule = getOrCreateRoutine(user,routine,date,dto);

        // 4. 리마인드 저장
        scheduleReminderRepository.deleteByScheduleId(schedule.getId());
        if (dto.getRemindAlarm() != null && !dto.getRemindAlarm().isEmpty()) {
            List<ScheduleReminder> reminders =
                    scheduleConverter.toScheduleReminders(schedule, dto.getRemindAlarm());
            scheduleReminderRepository.saveAll(reminders);
        }
        return new HomeResponseDto.TodoIdDto(schedule.getId());
    }

    @Transactional
    public Schedule getOrCreateRoutine(User user, Routine routine, LocalDate date, HomeRequestDto.TodoRequestDto dto) {
        // 1. 이미 존재하는지 먼저 조회
        Optional<Schedule> existing = scheduleRepository.findByUserAndRoutineAndDate(user, routine, date);
        if (existing.isPresent()) {
            return existing.get();
        }

        // 2. 없으면 convert로 새로 생성
        Schedule newSchedule = scheduleConverter.toScheduleFromRoutine(dto, user, routine);

        try {
            // 저장
            return scheduleRepository.save(newSchedule);
        } catch (DataIntegrityViolationException e) {
            // 동시에 다른 요청이 먼저 저장했을 수도 있으니 다시 조회
            return scheduleRepository.findByUserAndRoutineAndDate(user, routine, date)
                    .orElseThrow(() -> new HomeException(HomeErrorStatus._SCHEDULE_NOT_FOUND));
        }
    }

    @Transactional
    public HomeResponseDto.TodoIdDto updateCurrentRoutine(HomeRequestDto.TodoRequestDto dto, Schedule schedule) {
        // 스케줄테이블의 루틴 수정 (과거 & 현재 & 이미 수정한 미래의 루틴)

        // 1. 루틴은 상세 필드 수정 불가
        if(dto.getTitle() != null && !dto.getTitle().equals(schedule.getTitle())) {
            throw new HomeException(HomeErrorStatus._ROUTINE_FIELDS_IMMUTABLE);
        }
        if (dto.getDescription() != null && !dto.getDescription().equals(schedule.getDescription())) {
            throw new HomeException(HomeErrorStatus._ROUTINE_FIELDS_IMMUTABLE);
        }
        if (dto.getIsPublic() != null && !dto.getIsPublic().equals(schedule.getIsPublic())) {
            throw new HomeException(HomeErrorStatus._ROUTINE_FIELDS_IMMUTABLE);
        }
        if (dto.getIncludeTeum() != null && !dto.getIncludeTeum().equals(schedule.getIncludeTeum())) {
            throw new HomeException(HomeErrorStatus._ROUTINE_FIELDS_IMMUTABLE);
        }
        if (dto.getStartTime() != null && !dto.getStartTime().isEqual(schedule.getStartTime())) {
            throw new HomeException(HomeErrorStatus._ROUTINE_FIELDS_IMMUTABLE);
        }
        if (dto.getEndTime() != null && !dto.getEndTime().isEqual(schedule.getEndTime())) {
            throw new HomeException(HomeErrorStatus._ROUTINE_FIELDS_IMMUTABLE);
        }

        // 2. 필드 업데이트
        schedule.setRoutineStatus(RoutineStatus.MODIFIED);

        // 3. 리마인더 생성
        scheduleReminderRepository.deleteByScheduleId(schedule.getId());
        if (dto.getRemindAlarm() != null && !dto.getRemindAlarm().isEmpty()) {
            List<ScheduleReminder> reminders =
                    scheduleConverter.toScheduleReminders(schedule, dto.getRemindAlarm());
            scheduleReminderRepository.saveAll(reminders);
        }
        return new HomeResponseDto.TodoIdDto(schedule.getId());
    }

    @Transactional
    @Override
    public void deleteTodo(Long scheduleId) {
        // Todo(Schedule) 삭제

        // 1. 가상 루틴 ID인 경우
        if (scheduleId < 0) {
            // 반복일정 처리
            deleteVirtualRoutine(scheduleId);
            return;
        }

        // 2. 일반 스케줄 조회
        Schedule schedule = scheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new HomeException(HomeErrorStatus._SCHEDULE_NOT_FOUND));

        // 2-1. 루틴 기반 스케줄일 경우 -> routine_status = MODIFIED
        if (schedule.getRoutine() != null) {
            schedule.setRoutineStatus(RoutineStatus.DELETED);
            return;
        }

        scheduleRepository.deleteById(scheduleId);
        scheduleReminderRepository.deleteByScheduleId(scheduleId);
    }

    private void deleteVirtualRoutine(Long scheduleId) {
        // 가상의 루틴 ID 반복일정 삭제시
        HomeResponseDto.VirtualRoutineDto info = getVirtualRoutine(scheduleId);
        LocalDate date = info.getDate();
        Long routineId = info.getRoutineId();

        Routine routine = routineRepository.findById(routineId)
                .orElseThrow(() -> new HomeException(HomeErrorStatus._ROUTINE_NOT_FOUND));

        Schedule deletedSchedule = scheduleConverter.toDeleteRoutine(routine,date);
        scheduleRepository.save(deletedSchedule);
    }

    @Transactional
    @Override
    public void createWish(WishRequestDto.CreateDto dto, User user) {
        // Wish 등록

        // 카테고리 ID 유효성 검사
        List<Category> categories = categoryRepository.findAllById(dto.getCategories());
        if(categories.size () != dto.getCategories().size()){
            throw new HomeException(HomeErrorStatus._CATEGORY_NOT_FOUND);
        }

        // Wish & Wish Category 저장
        Wish wish = wishConverter.toWish(dto,user);
        List<WishCategory> wishCategories = wishConverter.toWishCategories(wish,categories);
        wish.setWishCategories(wishCategories);
        wishRepository.save(wish);
    }

    @Override
    public WishResponseDto.WishInfoDto getWishInfo(Long wishId) {
        // Wish 조회
        Wish wish = wishRepository.findById(wishId)
                .orElseThrow(() -> new HomeException(HomeErrorStatus._WISH_NOT_FOUND));
        return wishConverter.toWishInfoDTO(wish);
    }

    @Transactional
    @Override
    public void deleteWishByIds(WishRequestDto.WishDeleteDto dto) {
        // Wish 삭제
        List<Long> ids = dto.getWishIds();
        List<Wish> wishes = wishRepository.findAllById(ids);

        // 존재하지 않는 ID가 있는 경우 예외 처리
        if (wishes.size() != ids.size()) {
            throw new HomeException(HomeErrorStatus._WISH_NOT_FOUND);
        }

        // 삭제
        wishRepository.deleteAll(wishes);
    }

    @Transactional
    @Override
    public void updateWishInfo(WishRequestDto.CreateDto dto, Long wishId) {
        // Wish 수정
        Wish wish = wishRepository.findById(wishId)
                .orElseThrow(() -> new HomeException(HomeErrorStatus._WISH_NOT_FOUND));

        // 카테고리 ID 유효성 검사
        List<Category> categories = categoryRepository.findAllById(dto.getCategories());
        if(categories.size () != dto.getCategories().size()){
            throw new HomeException(HomeErrorStatus._CATEGORY_NOT_FOUND);
        }

        wish.getWishCategories().clear(); // 기존 wish category 정보 제거
        // 새로운 WishCategory 저장 & Wish 필드 업데이트
        List<WishCategory> wishCategories = wishConverter.toWishCategories(wish, categories);
        wish.getWishCategories().addAll(wishCategories);
        wish.update(
                dto.getTitle(),
                dto.getContent(),
                dto.getEstimatedDuration()
        );

    }

    @Override
    public WishResponseDto.WishlistDto getWishlist(String duration, Integer page, User user) {
        // Wishlist 조회
        System.out.println("user = " + user);
        // 페이징 조건:  page는 1부터, pageSize = 10, 정렬조건 = 최신순
        int pageSize = 10;
        Pageable pageable = PageRequest.of(page-1, pageSize, Sort.by("createdAt").descending());
        Slice<Wish> wishes;

        // duration 조건 분기
        if("all".equals(duration)){
            wishes = wishRepository.findAllByUser(user, pageable);
        } else {
            EstimatedDuration estimatedDuration = EstimatedDuration.from(duration); // enum 타입으로
            if (estimatedDuration == null) {
                throw new HomeException(HomeErrorStatus._INVALID_DURATION); // 잘못된 enum 타입
            }
            wishes = wishRepository.findAllByUserAndEstimatedDuration(user, estimatedDuration, pageable);
        }

        // DTO 변환
        return wishConverter.toWishlistResponseDTO(
                wishes.getContent(),
                page,
                pageSize,
                wishes.hasNext(),
                wishes.isFirst(),
                wishes.isLast()
        );
    }

    @Override
    public List<HomeResponseDto.TodayScheduleDto> getTodaySchedule(LocalDate date, User user) {
        // 오늘의 시간표 조회
        List<HomeResponseDto.TodayScheduleDto> sleepAndTodo = new ArrayList<>(); // 수면패턴과 투두를 등록한 배열

        // 1. 수면 패턴 등록
        LocalTime sleepTime = user.getSleepTime();
        LocalTime wakeTime = user.getWakeTime();

        if(sleepTime != null && wakeTime != null){
            if(sleepTime.isAfter(wakeTime)){
                // 자정 이전에 자는 경우
                sleepAndTodo.add(new HomeResponseDto.TodayScheduleDto(LocalTime.MIDNIGHT,wakeTime,"SLEEP"));
                sleepAndTodo.add(new HomeResponseDto.TodayScheduleDto(sleepTime,LocalTime.MAX,"SLEEP"));
            } else{
                // 자정 이후에 자는 경우
                sleepAndTodo.add(new HomeResponseDto.TodayScheduleDto(sleepTime,wakeTime,"SLEEP"));
            }
        }

        // 2. 스케줄 정보 등록
        LocalDateTime today = date.atStartOfDay(); // 오늘 자정
        LocalDateTime tomorrow = date.plusDays(1).atStartOfDay(); // 내일 자정

        // 다음날 자정보다 먼저 시작하는 일정 & 오늘 자정보다 늦게 끝나는 일정
        List<Schedule> schedules = scheduleRepository.findSchedulesOnDate(user, today, tomorrow);

        for (Schedule schedule : schedules) {
            // 삭제된 루틴 ->  표시 X
            if(schedule.getRoutine() != null && schedule.getIsDeleted()) continue;

            // 취소된 틈 -> 표시 X
            if(schedule.getStatus() == ScheduleStatus.CANCELLED) continue;

            // 시작날짜가 어제인 경우
            // 전날 시작되었다면 midnight부터 시작되도록 보정
            LocalTime start = schedule.getStartTime().isBefore(today)?
                    LocalTime.MIDNIGHT : schedule.getStartTime().toLocalTime();

            // 종료날짜가 내일인 경우
            // 종료시간이 내일 0시 이전이면 그대로 / 내일 0시 이후면 LocalTime MAX로
            LocalTime end = schedule.getEndTime().isBefore(tomorrow)
                    ? schedule.getEndTime().toLocalTime() : LocalTime.MAX;

            sleepAndTodo.add(HomeResponseDto.TodayScheduleDto.builder()
                    .startTime(start)
                    .endTime(end)
                    .type("TODO")
                    .build());
        }

        // 3. sleepAndTodo startTime 기준 정렬
        sleepAndTodo.sort(Comparator.comparing(HomeResponseDto.TodayScheduleDto::getStartTime));

        // 3-1. 겹치는 투두 병합
        sleepAndTodo = mergeSameTime(sleepAndTodo);

        // 4. EMPTY 채우기
        List<HomeResponseDto.TodayScheduleDto> result = new ArrayList<>(); // 응답 배열
        LocalTime pointer = LocalTime.MIDNIGHT;

        for (HomeResponseDto.TodayScheduleDto dto : sleepAndTodo) {

            if (pointer.isBefore(dto.getStartTime())) {
                // 빈틈이 존재하면 EMPTY 추가
                result.add(new HomeResponseDto.TodayScheduleDto(pointer, dto.getStartTime(), "EMPTY"));
            }

            result.add(dto);

            if (dto.getEndTime().isAfter(pointer)) {
                // 포인터 뒤에 일정이 있다면 포인터 갱신
                pointer = dto.getEndTime();
            }
        }

        // 5. 남은 시간 마지막 EMPTY 채우기
        if (pointer.isBefore(LocalTime.MAX)) {
            result.add(new HomeResponseDto.TodayScheduleDto(pointer, LocalTime.MAX, "EMPTY"));
        }

        return result;
    }

    private List<HomeResponseDto.TodayScheduleDto> mergeSameTime(List<HomeResponseDto.TodayScheduleDto> list) {
        // 투두 겹침 처리 로직
        if(list.isEmpty()) return list;

        List<HomeResponseDto.TodayScheduleDto> result = new ArrayList<>();
        HomeResponseDto.TodayScheduleDto last = list.get(0); // 첫번째 투두
        result.add(last);

        for(int i = 1; i < list.size(); i++){
            HomeResponseDto.TodayScheduleDto current = list.get(i);

            if(last.getType().equals(current.getType()) &&
                    !current.getStartTime().isAfter(last.getEndTime())){
                // current 투두의 시작시간이 last 투두의 종료시간과 같거나 이른 경우

                // 둘중 늦은 endTime 으로 설정
                LocalTime endTime = last.getEndTime().isAfter(current.getEndTime()) ? last.getEndTime() : current.getEndTime();

                HomeResponseDto.TodayScheduleDto merged = HomeResponseDto.TodayScheduleDto.builder()
                        .startTime(last.getStartTime()) // 이전 시작
                        .endTime(endTime) // 현재 종료
                        .type(last.getType())
                        .build();

                result.set(result.size() - 1, merged); // last를 merge로 바꿈
                last = merged; // last 업데이트
            } else{
                result.add(current); // current 넣기
                last = current; // last 업데이트
            }
        }
        return result;
    }

    @Override
    public void assignWish(Long wishId, WishRequestDto.WishAssignDto dto, User user) {
        // 위시 투두 등록

        // 1. 위시 조회
        Wish wish = wishRepository.findById(wishId)
                .orElseThrow(() -> new HomeException(HomeErrorStatus._WISH_NOT_FOUND));

        // 2. 시간 유효성 검사
        if(dto.getEndTime().isBefore(dto.getStartTime()) || dto.getEndTime().isEqual(dto.getStartTime())){
            throw new HomeException(HomeErrorStatus._INVALID_TIME_RANGE);
        }

        // 3 중복 스케줄 체크
        // 3-1. 틈 & 수면패턴 중복 검사 -> 등록 불가
        conflictValidator.validateTodo(user,dto.getStartTime(),dto.getEndTime());

        // 3-2. 스케줄 중복 검사 -> 등록 가능
        LocalDate date = dto.getStartTime().toLocalDate();
        LocalDateTime startTime = dto.getStartTime();
        LocalDateTime endTime = dto.getEndTime();

        boolean hasConflict = scheduleRepository.existsConflictSchedule(
                user.getId(), date, startTime, endTime
        );

        if (hasConflict && !dto.getIsForce()) {
            // force가 false고 일정이 겹치면 예외
            throw new HomeException(HomeErrorStatus._SCHEDULE_CONFLICT);
        }

        // 4. 스케줄 생성 & 위시 삭제
        Schedule schedule = scheduleConverter.toScheduleFromWish(wish,dto);
        scheduleRepository.save(schedule);
        wishRepository.delete(wish);
    }

    @Override
    public List<WishResponseDto.CategoryDto> getCategory() {
        // 카테고리 정보 조회
        List<Category> categories = categoryRepository.findAll();
        return wishConverter.toCategoryResponseDTO(categories);
    }

    @Override
    public HomeResponseDto.TeumTimeDto getTeaumTime(User user) {
        // 채움(AI), 위시, 투두, 틈약속 모두 Schedule에 저장됨.
        // 요청을 보낸 시간 이전의 schedule의 enddate들만 확인.. (활동을 수행했다고 판정하는 기준)
        // schedule의 includeTeum을 확인해야함
        // 그리고 startTime ~ endTime을 계산

        // 1. 현재 시간 조회
        LocalDateTime now = LocalDateTime.now();

        // 2. endDateTime이 현재 시간 이전이고 && 해당 유저에 해당하는 스케줄들 조회 && IncludeTeaum이 True
        List<Schedule> scheduleList = scheduleRepository.findAllByUserAndIncludeTeumTrueAndEndTimeBefore(user, now);

        // 3. 수면패턴과 반복일정은 틈 시간조회에 포함 대상이 아니므로, 제외하기 위해 Set을 만듬.
        Set<ScheduleType> validTypes = Set.of(
            ScheduleType.TEUM,
            ScheduleType.WISH,
            ScheduleType.AI,
            ScheduleType.TODO
        );

        // 4. 시간 계산
        Duration totalDuration = Duration.ZERO;

        for (Schedule schedule : scheduleList) {
            // 4-1. 의미 있는 스케줄 타입만 처리
            if(!validTypes.contains(schedule.getType())){
                continue;
            }
            // 4-2. 만약 TEUM 이라면 COMPLETED 상태인지 확인 (CANCLE 상태이면 틈 시간에 포함하지 않음)
            if (schedule.getType() == ScheduleType.TEUM) {
                if(schedule.getStatus() != ScheduleStatus.COMPLETED){
                    continue;
                }

            // 4-3. 만약 TEUM 이 아니라면 -> (AI, WISH, TODO, TEUM 라면) ACTIVE 상태의 스케줄만 체크
            }else{
                if(schedule.getStatus() != ScheduleStatus.ACTIVE){
                    continue;
                }
            }

            Duration duration = Duration.between(schedule.getStartTime(), schedule.getEndTime());
            totalDuration = totalDuration.plus(duration);

        }
        return scheduleConverter.toTeumTimeDto(totalDuration);


    }

    @Override
    public List<HomeResponseDto.CalendarDto> getCalendar(LocalDate startDate, LocalDate endDate, User user) {
        // 캘린더 조회

        // 1. 오늘 날짜 조회 & 날짜별 일정 여부 담을 맵 초기화
        LocalDate today = LocalDate.now();
        Map<LocalDate, Boolean> calendarMap = new HashMap<>();

        // 2. Schedule 테이블 조회
        List<Schedule> schedules = scheduleRepository.findByUserAndDateBetween(user,startDate,endDate);

        // 3. Schedule 기준 true 표시
        for (Schedule schedule : schedules) {
            LocalDate date = schedule.getDate();
            // 3-1. 삭제된 루틴 ->  표시 X
            if(schedule.getRoutine() != null && schedule.getIsDeleted()) continue;

            // 3-2. 취소된 틈 -> 표시 X
            if(schedule.getStatus() == ScheduleStatus.CANCELLED) continue;

            // 3-3. 일정 등록
            calendarMap.put(date, true);
        }


        // 4. 미래의 경우 반복일정 검증
        if(endDate.isAfter(today)){

            // 4-1. 스케줄에서 삭제된 날짜의 루틴ID만 모아둠
            Map<LocalDate, Set<Long>> deletedRoutine = schedules.stream()
                    .filter(s -> s.getRoutine() != null && s.getIsDeleted())
                    .collect(Collectors.groupingBy(
                            Schedule::getDate,
                            Collectors.mapping(
                                    s -> s.getRoutine().getId(),
                                    Collectors.toSet()
                            )
                    ));

            // 4-2. 반복 일정 조회
            List<Routine> routines = routineRepository.findByUser(user);

            // 4-3. startDate부터 endDate까지 날짜 순회
            for(LocalDate date = startDate; !date.isAfter(endDate); date = date.plusDays(1)) {
                // 과거일 경우는 스킵
                if(date.isBefore(today)) continue;

                // 검증 날짜의 요일
                Weekday weekday = Weekday.from(date.getDayOfWeek());

                for(Routine routine : routines) {
                    // 루틴의 요일과 검증 날짜의 요일이 일치하는 경우만 처리
                    if(!routine.getWeekday().equals(weekday)) {
                        continue;
                    }

                    // 삭제되지 않은 루틴만 true로
                    Long routineId = routine.getId();
                    boolean isDeleted = deletedRoutine.getOrDefault(date,Set.of()).contains(routineId);
                    if(!isDeleted){
                        calendarMap.put(date, true);
                    }
                }
            }

        }
        return scheduleConverter.toCalendarDto(calendarMap,startDate,endDate);
    }

    @Override
    public List<HomeResponseDto.TodolistDto> getTodolist(LocalDate date, User user) {
        // 투두리스트 조회

        // 1. 해당 날짜의 모든 스케줄 조회
        List<Schedule> allSchedules = scheduleRepository.findByUserAndDate(user,date);

        // 2. 삭제되지 않은 스케줄 필터링 & 삭제된 틈 필터링
        List<Schedule> validSchedules = allSchedules.stream()
                .filter(s -> !s.getIsDeleted())
                .filter(s -> !s.getStatus().equals(ScheduleStatus.CANCELLED))
                .toList();

        // 3. 알림 상태 판단을 위해,, ScheduleReminder 조회
        List<Long> scheduleIds = validSchedules.stream()
                .map(Schedule::getId)
                .toList();

        List<ScheduleReminder> reminders = scheduleReminderRepository.findByScheduleIdIn(scheduleIds);

        Map<Long, List<ScheduleReminder>> reminderMap = reminders.stream()
                .collect(Collectors.groupingBy(r -> r.getSchedule().getId()));

        // 4. 스케줄 DTO 변환 (해당 날짜 & 삭제되지 않음)
        List<HomeResponseDto.TodolistDto> scheduleDtos = validSchedules.stream()
                .map(schedule -> {
                    List<ScheduleReminder> scheduleReminders = reminderMap.getOrDefault(schedule.getId(), Collections.emptyList());
                    AlarmStatus alarmStatus;
                    if(scheduleReminders.isEmpty()){
                        alarmStatus = AlarmStatus.NONE;
                    } else if(scheduleReminders.stream().anyMatch(r -> r.getAlarmStatus() == AlarmStatus.ACTIVE)){
                        alarmStatus = AlarmStatus.ACTIVE;
                    } else if(schedule.getType() == ScheduleType.ROUTINE) {
                        // 데모데이까지 반복일정의 경우 알림은 NONE 수정불가
                        alarmStatus = AlarmStatus.NONE;
                    } else{
                        alarmStatus = AlarmStatus.INACTIVE;
                    }
                    return scheduleConverter.toScheduleDto(schedule,alarmStatus);
                })
                .toList();

        // 5. 삭제된 루틴 ID
        Set<Long> deletedRoutineIds = allSchedules.stream()
                .filter(s-> s.getRoutine() != null && s.getIsDeleted())
                .map(s->s.getRoutine().getId())
                .collect(Collectors.toSet());

        // 6. 미래의 경우 반복 루틴 추가 조회
        LocalDate today = LocalDate.now();
        List<HomeResponseDto.TodolistDto> routineDtos = new ArrayList<>();
        if(date.isAfter(today)){
            // 6-1. 조회 요일
            Weekday todayWeekday = Weekday.valueOf(date.getDayOfWeek().name());
            List<Routine> routines = routineRepository.findByUserAndWeekday(user, todayWeekday);

            // 6-2. 삭제되지 않은 루틴에 대해 가상의 ID 생성
            routineDtos = routines.stream()
                    .filter(r -> !deletedRoutineIds.contains(r.getId()))
                    .map(r -> scheduleConverter.toVirtualRoutineDto(r, date))
                    .toList();
        }

        // 7. 합쳐서 반환 (시간순 정렬)
        List<HomeResponseDto.TodolistDto> result = new ArrayList<>();
        result.addAll(scheduleDtos);
        result.addAll(routineDtos);
        result.sort(Comparator.comparing(HomeResponseDto.TodolistDto::getStartTime));
        return result;
    }

    @Override
    public HomeResponseDto.VirtualRoutineDto getVirtualRoutine(Long virtualId) {
        // 가상의 루틴 ID 파싱 함수

        try{
            String str = Long.toString(Math.abs(virtualId)); // 음수 제거 후 string으로

            if(str.length()<=8){
                throw new IllegalArgumentException();
            }

            // 날짜 파싱
            String strDate = str.substring(0,8); //YYYYMMDD
            String strRoutine = str.substring(8);

            // 루틴 ID 파싱
            LocalDate date = LocalDate.parse(strDate, DateTimeFormatter.ofPattern("yyyyMMdd"));
            Long routineId = Long.parseLong(strRoutine);
            return new HomeResponseDto.VirtualRoutineDto(date, routineId);
        } catch(Exception e){
            throw new HomeException(HomeErrorStatus._INVALID_VIRTUAL_ID);
        }
    }

    @Override
    public HomeResponseDto.ReminderDto getUserRemind(User user) {
        // 리마인드 알림 정보 조회
        List<Integer> response = remindAlarmRepository.findAllByUser(user).stream()
                .map(RemindAlarm::getMinutesBefore)
                .toList();

        return HomeResponseDto.ReminderDto.builder()
                .reminders(response).build();
    }

    @Transactional
    @Override
    public void updateAlarm(HomeRequestDto.AlarmDto dto) {
        // 리마인드 알림 정보 변경
        Long scheduleId = dto.getTodoId();
        AlarmStatus alarmStatus = dto.getAlarmStatus();

        if (scheduleId < 0) {
            // 1. 미래의 반복일정은 수정할 수 없음
            throw new HomeException(HomeErrorStatus._CANNOT_UPDATE_ROUTINE);
        }

        // 2. 스케줄 알림 테이블 조회
        List<ScheduleReminder> reminders = scheduleReminderRepository.findByScheduleId(scheduleId);

        // 3. 필드 업데이트
        for(ScheduleReminder reminder : reminders){
            reminder.updateStatus(alarmStatus);
        }
    }
}
