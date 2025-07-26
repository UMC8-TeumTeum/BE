package umc.teumteum.server.domain.home.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import umc.teumteum.server.domain.home.converter.ScheduleConverter;
import umc.teumteum.server.domain.home.converter.WishConverter;
import umc.teumteum.server.domain.home.dto.request.TodoRequestDTO;
import umc.teumteum.server.domain.home.dto.request.WishAssignRequestDTO;
import umc.teumteum.server.domain.home.dto.request.WishDeleteRequestDTO;
import umc.teumteum.server.domain.home.dto.request.WishRequestDTO;
import umc.teumteum.server.domain.home.dto.response.*;
import umc.teumteum.server.domain.home.entity.Category;
import umc.teumteum.server.domain.home.entity.Schedule;
import umc.teumteum.server.domain.home.entity.ScheduleReminder;
import umc.teumteum.server.domain.home.entity.Wish;
import umc.teumteum.server.domain.home.entity.enums.EstimatedDuration;
import umc.teumteum.server.domain.home.entity.enums.ScheduleType;
import umc.teumteum.server.domain.home.entity.mapping.WishCategory;
import umc.teumteum.server.domain.home.exception.status.HomeErrorStatus;
import umc.teumteum.server.domain.home.exception.HomeException;
import umc.teumteum.server.domain.home.repository.*;
import umc.teumteum.server.domain.teum.entity.TeumRequest;
import umc.teumteum.server.domain.teum.entity.enums.ResponseStatus;
import umc.teumteum.server.domain.teum.exception.status.TeumErrorStatus;
import umc.teumteum.server.domain.teum.repository.TeumRequestRepository;
import umc.teumteum.server.domain.user.entity.User;
import umc.teumteum.server.domain.user.repository.UserRepository;
import umc.teumteum.server.global.exception.GeneralException;
import umc.teumteum.server.global.util.S3Util;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
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

    @Transactional
    @Override
    public TodoIdResponseDTO createTodo(TodoRequestDTO dto, User user) {
        // Todo 등록
        // 종료 시간이 시작 시간보다 빠르면 예외 발생
        if (dto.getEndTime().isBefore(dto.getStartTime())){
            throw new HomeException(HomeErrorStatus._INVALID_TIME_RANGE);
        }

        // 스케줄 저장
        Schedule schedule = scheduleConverter.toSchedule(dto,user);
        Schedule savedSchedule = scheduleRepository.save(schedule);

        // 스케줄 리마인드 알림 저장
        if (dto.getRemindAlarm() != null && !dto.getRemindAlarm().isEmpty()) {
            List<ScheduleReminder> reminders = scheduleConverter.toScheduleReminders(savedSchedule, dto.getRemindAlarm());
            scheduleReminderRepository.saveAll(reminders);
        }
        return new TodoIdResponseDTO(savedSchedule.getId());
    }

    @Override
    public TodoInfoResponseDTO getTodoInfo(Long scheduleId) {
        // Todo(Schedule) 조회
        Schedule schedule = scheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new HomeException(HomeErrorStatus._SCHEDULE_NOT_FOUND));

        // 라미인드 알림 조회
        List<ScheduleReminder> reminders = scheduleReminderRepository.findByScheduleId(schedule.getId());

        // 프로필 조회
        List<String> profileUrls;
        if(schedule.getType() == ScheduleType.TEUM){ // TEUM 타입인 경우
            profileUrls = getTeumProfileUrls(schedule);
        } else{
            String profileImageKey = schedule.getUser().getProfileImageKey();
            profileUrls = profileImageKey != null ? List.of(s3Util.toPresignedUrl(profileImageKey, Duration.ofMinutes(30))) : List.of();
        }

        return scheduleConverter.toTodoInfoResponse(schedule,reminders, profileUrls);
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
                .map(User::getProfileImageKey)
                .filter(Objects::nonNull)
                .map(key -> s3Util.toPresignedUrl(key, Duration.ofMinutes(30)))
                .toList();
    }

    @Transactional
    @Override
    public TodoIdResponseDTO updateTodoInfo(TodoRequestDTO dto, Long scheduleId) {
        // Todo(Schedule) 수정
        Schedule schedule = scheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new HomeException(HomeErrorStatus._SCHEDULE_NOT_FOUND));

        // 종료 시간이 시작 시간보다 빠르면 예외 발생
        if (dto.getEndTime().isBefore(dto.getStartTime())) {
            throw new HomeException(HomeErrorStatus._INVALID_TIME_RANGE);
        }

        // 스케줄 필드 업데이트
        schedule.updateField(dto);

        // 스케줄 리마인드 알림 저장
        if (dto.getRemindAlarm() != null && !dto.getRemindAlarm().isEmpty()) {
            scheduleReminderRepository.deleteByScheduleId(scheduleId);
            List<ScheduleReminder> reminders = scheduleConverter.toScheduleReminders(schedule, dto.getRemindAlarm());
            scheduleReminderRepository.saveAll(reminders);
        }
        return new TodoIdResponseDTO(schedule.getId());
    }

    @Transactional
    @Override
    public void deleteTodo(Long scheduleId) {
        // Todo(Schedule) 삭제
        Schedule schedule = scheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new HomeException(HomeErrorStatus._SCHEDULE_NOT_FOUND));

        scheduleRepository.deleteById(scheduleId);
        scheduleReminderRepository.deleteByScheduleId(scheduleId);
    }

    @Transactional
    @Override
    public void createWish(WishRequestDTO dto,User user) {
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
    public WishInfoResponseDTO getWishInfo(Long wishId) {
        // Wish 조회
        Wish wish = wishRepository.findById(wishId)
                .orElseThrow(() -> new HomeException(HomeErrorStatus._WISH_NOT_FOUND));
        return wishConverter.toWishInfoDTO(wish);
    }

    @Transactional
    @Override
    public void deleteWishByIds(WishDeleteRequestDTO dto) {
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
    public void updateWishInfo(WishRequestDTO dto, Long wishId, User user) {
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
    public WishlistResponseDTO getWishlist(String duration, Integer page, User user) {
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
    public List<TodayScheduleResponseDTO> getTodaySchedule(LocalDate date, User user) {
        // 오늘의 시간표 조회
        List<TodayScheduleResponseDTO> sleepAndTodo = new ArrayList<>(); // 수면패턴과 투두를 등록한 배열

        // 1. 수면 패턴 등록
        LocalTime sleepTime = user.getSleepTime();
        LocalTime wakeTime = user.getWakeTime();

        if(sleepTime != null && wakeTime != null){
            if(sleepTime.isAfter(wakeTime)){
                // 자정 이전에 자는 경우
                sleepAndTodo.add(new TodayScheduleResponseDTO(LocalTime.MIDNIGHT,wakeTime,"SLEEP"));
                sleepAndTodo.add(new TodayScheduleResponseDTO(sleepTime,LocalTime.MAX,"SLEEP"));
            } else{
                // 자정 이후에 자는 경우
                sleepAndTodo.add(new TodayScheduleResponseDTO(sleepTime,wakeTime,"SLEEP"));
            }
        }

        // 2. 스케줄 정보 등록
        LocalDateTime today = date.atStartOfDay(); // 오늘 자정
        LocalDateTime tomorrow = date.plusDays(1).atStartOfDay(); // 내일 자정

        // 다음날 자정보다 먼저 시작하는 일정 & 오늘 자정보다 늦게 끝나는 일정
        List<Schedule> schedules = scheduleRepository.findSchedulesOnDate(user, today, tomorrow);

        for (Schedule schedule : schedules) {
            // 시작날짜가 어제인 경우
            LocalTime start = schedule.getStartTime().isBefore(today)?
                    LocalTime.MIDNIGHT : schedule.getStartTime().toLocalTime();

            // 종료날짜가 내일인 경우
            LocalTime end = schedule.getEndTime().isAfter(tomorrow)?
                    LocalTime.MAX : schedule.getEndTime().toLocalTime();

            sleepAndTodo.add(TodayScheduleResponseDTO.builder()
                    .startTime(start)
                    .endTime(end)
                    .type("TODO")
                    .build());
        }

        // 3. sleepAndTodo startTime 기준 정렬
        sleepAndTodo.sort(Comparator.comparing(TodayScheduleResponseDTO::getStartTime));

        // 4. EMPTY 채우기
        List<TodayScheduleResponseDTO> result = new ArrayList<>(); // 응답 배열
        LocalTime pointer = LocalTime.MIDNIGHT;

        for (TodayScheduleResponseDTO dto : sleepAndTodo) {

            if (pointer.isBefore(dto.getStartTime())) {
                // 빈틈이 존재하면 EMPTY 추가
                result.add(new TodayScheduleResponseDTO(pointer, dto.getStartTime(), "EMPTY"));
            }

            result.add(dto);

            if (dto.getEndTime().isAfter(pointer)) {
                // 포인터 뒤에 일정이 있다면 포인터 갱신
                pointer = dto.getEndTime();
            }
        }

        // 5. 남은 시간 마지막 EMPTY 채우기
        if (pointer.isBefore(LocalTime.MAX)) {
            result.add(new TodayScheduleResponseDTO(pointer, LocalTime.MAX, "EMPTY"));
        }

        return result;
    }

    @Override
    public void assignWish(Long wishId, WishAssignRequestDTO dto, User user) {
        // 위시 투두 등록

        // 1. 위시 조회
        Wish wish = wishRepository.findById(wishId)
                .orElseThrow(() -> new HomeException(HomeErrorStatus._WISH_NOT_FOUND));

        // 2. 중복 스케줄 체크
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

        // 3. 스케줄 생성 & 위시 삭제
        Schedule schedule = scheduleConverter.toScheduleFromWish(wish,dto);
        scheduleRepository.save(schedule);
        wishRepository.delete(wish);
    }
}
