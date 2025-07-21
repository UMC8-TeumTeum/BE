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
import umc.teumteum.server.domain.home.dto.*;
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
    public TodoIdResponseDTO createTodo(TodoRequestDTO dto) {
        // Todo 등록
        // 종료 시간이 시작 시간보다 빠르면 예외 발생
        if (dto.getEndTime().isBefore(dto.getStartTime())){
            throw new HomeException(HomeErrorStatus._INVALID_TIME_RANGE);
        }

        // 스케줄 저장
        Schedule schedule = scheduleConverter.toSchedule(dto);
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
    public void createWish(WishRequestDTO dto) {
        // Wish 등록
        User user = userRepository.getReferenceById(dto.getUserId()); // 시큐리티 적용후 변경 예정

        // 카테고리 ID 유효성 검사
        List<Category> categories = categoryRepository.findAllById(dto.getCategories());
        if(categories.size () != dto.getCategories().size()){
            throw new HomeException(HomeErrorStatus._CATEGORY_NOT_FOUND);
        }

        // 동일한 wish가 이미 존재하는지 확인
        if (isDuplicateWish(user, dto, null)) {
            throw new HomeException(HomeErrorStatus._WISH_CONFLICT);
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
    public void updateWishInfo(WishRequestDTO dto, Long wishId) {
        // Wish 수정
        User user = userRepository.getReferenceById(dto.getUserId()); // 시큐리티 적용후 변경 예정

        Wish wish = wishRepository.findById(wishId)
                .orElseThrow(() -> new HomeException(HomeErrorStatus._WISH_NOT_FOUND));

        // 카테고리 ID 유효성 검사
        List<Category> categories = categoryRepository.findAllById(dto.getCategories());
        if(categories.size () != dto.getCategories().size()){
            throw new HomeException(HomeErrorStatus._CATEGORY_NOT_FOUND);
        }

        // 동일한 wish가 이미 존재하는지 확인
        if (isDuplicateWish(user, dto, wishId)) {
            throw new HomeException(HomeErrorStatus._WISH_CONFLICT);
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

    private boolean isDuplicateWish(User user, WishRequestDTO dto, Long currentWishId) {
        // 중복 검사
        // user, title, content, duration이 같은 wish
        List<Wish> candidates = wishRepository.findByUserAndTitleAndContentAndEstimatedDuration(
                user, dto.getTitle(), dto.getContent(), dto.getEstimatedDuration()
        );

        for (Wish candidate : candidates) {
            // 현재 wish id(자기자신) 제외
            if (currentWishId != null && currentWishId.equals(candidate.getId())) continue;

            // 카테고리 ID 목록 비교
            Set<Long> dtoCategoryIds = new HashSet<>(dto.getCategories());

            Set<Long> candidateCategoryIds = candidate.getWishCategories().stream()
                    .map(wc -> wc.getCategory().getId())
                    .collect(Collectors.toSet());

            if (dtoCategoryIds.equals(candidateCategoryIds)) {
                return true;
            } // 카테고리까지 동일하다면 중복
        }
        return false;
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
}
