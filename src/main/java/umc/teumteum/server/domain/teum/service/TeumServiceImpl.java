package umc.teumteum.server.domain.teum.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import umc.teumteum.server.domain.friend.exception.status.FriendErrorStatus;
import umc.teumteum.server.domain.friend.repository.BlockRepository;
import umc.teumteum.server.domain.home.entity.Schedule;
import umc.teumteum.server.domain.home.entity.enums.RoutineStatus;
import umc.teumteum.server.domain.home.entity.enums.ScheduleStatus;
import umc.teumteum.server.domain.home.entity.enums.ScheduleType;
import umc.teumteum.server.domain.home.repository.ScheduleRepository;
import umc.teumteum.server.domain.notification.service.NotificationUseCases;
import umc.teumteum.server.domain.teum.converter.TeumConverter;
import umc.teumteum.server.domain.teum.dto.TeumRequestDto;
import umc.teumteum.server.domain.teum.dto.TeumResponseDto;
import umc.teumteum.server.domain.teum.dto.common.TimeSlot;
import umc.teumteum.server.domain.teum.entity.TeumRequest;
import umc.teumteum.server.domain.teum.entity.TeumResponse;
import umc.teumteum.server.domain.teum.entity.enums.RequestStatus;
import umc.teumteum.server.domain.teum.entity.enums.ResponseStatus;
import umc.teumteum.server.domain.teum.exception.status.TeumErrorStatus;
import umc.teumteum.server.domain.teum.repository.TeumRequestRepository;
import umc.teumteum.server.domain.teum.repository.TeumResponseRepository;
import umc.teumteum.server.domain.user.entity.Routine;
import umc.teumteum.server.domain.user.entity.User;
import umc.teumteum.server.domain.user.entity.enums.UserStatus;
import umc.teumteum.server.domain.user.exception.status.UserErrorStatus;
import umc.teumteum.server.domain.user.repository.UserRepository;
import umc.teumteum.server.global.dto.PagingResponseDto;
import umc.teumteum.server.global.exception.GeneralException;
import umc.teumteum.server.global.exception.handler.GlobalHandler;
import umc.teumteum.server.global.util.S3Util;
import umc.teumteum.server.global.util.TimeUtil;
import umc.teumteum.server.global.validator.ConflictValidator;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
public class TeumServiceImpl implements TeumService {

    private final S3Util s3Util;
    private final ConflictValidator conflictValidator;

    private final UserRepository userRepository;
    private final TeumRequestRepository teumRequestRepository;
    private final TeumResponseRepository teumResponseRepository;
    private final ScheduleRepository scheduleRepository;
    private final BlockRepository blockRepository;

    private final TeumConverter teumConverter;
    private final TimeUtil timeUtil;
    private final NotificationUseCases notificationUseCases;

  private String toProfileUrl(User user) {
        if (user == null || user.getProfileImageName() == null) return null;
        return s3Util.toPresignedUrl("profile/" + user.getProfileImageName(), Duration.ofMinutes(30));
    }

    @Override
    @Transactional
    public Long createRequest(TeumRequestDto.TeumRequest dto, User user) {
        // 시간 순서 검증
        validateTimeOrder(dto.getStartTime(), dto.getEndTime());

        // 날짜 및 시간 파싱
        LocalDate date = LocalDate.parse(dto.getDate());
        LocalTime startTime = LocalTime.parse(dto.getStartTime());
        LocalTime endTime = LocalTime.parse(dto.getEndTime());

        // 시작 시간이 현재 시각보다 과거인지 검증
        validateStartTimeNotPast(date, startTime);

        // 수신자 조회 (단일 사용자)
        User receiver = getUserOrThrow(dto.getReceiverUserId());

        // 수신자가 ACTIVE 유저인지 검증
        validActiveUser(receiver);

        // 차단 관계 검증 (요청자 <-> 수신자)
        validateBlockRelationship(user, receiver);

        // 요청자와 수신자 둘 다 시간 충돌 여부 검증
        conflictValidator.validateTeumForUsers(
                List.of(user, receiver),   // 두 명만 비교
                date,
                startTime,
                endTime
        );

        // 요청 객체 생성 및 저장
        TeumRequest request = teumConverter.toTeumRequest(dto, user);
        TeumResponse response = teumConverter.toTeumResponse(
                dto.getReceiverUserId(),
                user.getId(),
                request,
                this::getUserOrThrow
        );

        request.getTeumResponses().add(response);
        teumRequestRepository.save(request);

      // [추가] 단일 수신자 알림 전송
        notificationUseCases.notifyTeumRequest(
                user,          // 요청자
                receiver,      // 단일 수신자
                request.getId(),
                dto
        );
        return request.getId();

    }

    @Override
    @Transactional
    public Long createResendRequest(Long parentRequestId, TeumRequestDto.TeumResend dto, User user) {
      // 재요청자 ACTIVE 검증
      validActiveUser(user);

      // 원본 요청 확인 및 권한 검증
        TeumRequest parent = findActiveRequestOrThrow(parentRequestId);
        validateResendableRequest(parent);
        validateResender(parent, user.getId());

        // 시간 순서 검증
        validateTimeOrder(dto.getStartTime(), dto.getEndTime());

        // 시간 충돌 검증 (요청자 + 수신자 모두)
        LocalDate date = parent.getDate();
        LocalTime startTime = LocalTime.parse(dto.getStartTime());
        LocalTime endTime = LocalTime.parse(dto.getEndTime());

        // 시작 시간이 현재 시각보다 과거인지 검증
        validateStartTimeNotPast(date, startTime);

        User originalSender = parent.getUser();  // 부모 요청의 작성자 → 이번 재요청의 수신자
        validActiveUser(originalSender); // 기존 요청자가 ACTIVE인지 검증

        // 차단 관계 검증 (재요청자 <-> 기존 요청자)
        validateBlockRelationship(user, originalSender);

        List<User> participants = List.of(user, originalSender);
        conflictValidator.validateTeumForUsers(participants, date, startTime, endTime);

        // 요청 및 응답 생성
        TeumRequest newRequest = teumConverter.toResendTeumRequest(parent, dto, user);
        TeumResponse newResponse = teumConverter.toResendTeumResponse(newRequest, originalSender);
        newRequest.getTeumResponses().add(newResponse);

        // 응답 상태 변경
        TeumResponse originalResponse = parent.getTeumResponses().getFirst();
        originalResponse.changeStatus(ResponseStatus.RESEND);

        teumRequestRepository.save(newRequest);

        // [추가] 알림 전송 1:1 재요청
        notificationUseCases.notifyTeumReRequest(
            user,
            originalSender,
            newRequest.getId(),
            newRequest
        );

      return newRequest.getId();
    }


    @Override
    @Transactional(readOnly = true)
    public Page<TeumResponseDto.TeumReceived> getReceivedRequests(Long userId, int page, int size) {
        getUserOrThrow(userId);

        int pageIndex = Math.max(page - 1, 0);

        Sort sort = Sort.by(
                Sort.Order.asc("readAt"),
                Sort.Order.desc("createdAt")
        );

        Pageable pageable = PageRequest.of(pageIndex, size, sort);

        Page<TeumResponse> pageData = teumResponseRepository.findValidPendingResponses(
                userId,
                LocalDate.now(),
                LocalTime.now(),
                ResponseStatus.PENDING,
                RequestStatus.ACTIVE,
                pageable
        );

        List<TeumResponseDto.TeumReceived> dtoList = pageData.getContent().stream()
                .map(response -> {
                    User sender = response.getTeumRequest().getUser();
                    String url = toProfileUrl(sender);
                    return teumConverter.toReceivedResponseDto(response, url);
                })
                .toList();

        return new PageImpl<>(dtoList, pageable, pageData.getTotalElements());
    }

    @Override
    @Transactional
    public Long updateReadStatus(Long responseId, Long userId) {
        TeumResponse response = getResponseOrThrow(responseId);
        validateReceiver(response, userId);

        // 차단 관계 검증 (읽으려는 사람 <-> 보낸 사람)
        User reader = response.getReceiverUser();
        User sender = response.getTeumRequest().getUser();

        validateBlockRelationship(reader, sender);

        response.markAsRead();
        return responseId;
    }

    @Override
    @Transactional
    public TeumResponseDto.TeumStatusUpdate updateResponseStatus(Long responseId, Long userId, TeumRequestDto.TeumStatusUpdate requestDto) {
        // 응답 조회 및 권한 검증
        TeumResponse response = getResponseOrThrow(responseId);
        validateReceiver(response, userId);

        TeumRequest request = response.getTeumRequest();
        User requester = request.getUser();      // 요청 보낸 사람
        User receiver = response.getReceiverUser(); // 나 (응답하는 사람)

        // 차단 관계 검증 (응답자 <-> 요청자)
        validateBlockRelationship(receiver, requester);

        // 이미 처리된 응답인 경우 예외
        if (response.getStatus() != ResponseStatus.PENDING) {
            throw new GeneralException(TeumErrorStatus.REQUEST_ALREADY_CLOSED);
        }

        // 요청된 응답 상태 유효성 검증
        ResponseStatus newStatus;
        try {
            newStatus = ResponseStatus.valueOf(requestDto.getStatus().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new GeneralException(TeumErrorStatus.INVALID_RESPONSE_STATUS);
        }

        // 상태 변경 적용
        response.changeStatus(newStatus);

        boolean isAccepted = newStatus == ResponseStatus.ACCEPTED;
        Long teumId = null;

        if (isAccepted) {
            // 스케줄 생성: 수신자(응답자)
            Schedule receiverSchedule = teumConverter.toScheduleFromTeumRequest(request, receiver);
            scheduleRepository.save(receiverSchedule);

            // 스케줄 생성: 요청자(없으면 생성)
            if (!scheduleRepository.existsByTeumRequestAndUser(request, requester)) {
                Schedule requesterSchedule = teumConverter.toScheduleFromTeumRequest(request, requester);
                scheduleRepository.save(requesterSchedule);
            }

            // 응답자 스케줄 ID 반환
            teumId = receiverSchedule.getId();

            // 1:1 확정 → 요청 종료
            request.markAsClosed();

        } else {
            if (newStatus == ResponseStatus.REJECTED || newStatus == ResponseStatus.LEFT) {
                request.markAsClosed();
            }
        }

        // 알림 (응답자 → 요청자)
        notificationUseCases.notifyTeumResponse(
                receiver,
                requester,
                response.getId(),
                isAccepted
        );

        // 응답 DTO
        return teumConverter.toStatusUpdateResponseDto(newStatus, isAccepted, teumId);
    }

    @Override
    public List<String> getTeumRequestsOfMonth(Long userId, String month) {
        YearMonth yearMonth = YearMonth.parse(month);
        LocalDate start = yearMonth.atDay(1);
        LocalDate end = yearMonth.atEndOfMonth();

        List<LocalDate> myRequestDates =
                teumRequestRepository.findMyRequestDates(userId, start, end);

        List<LocalDate> receivedRequestDates =
                teumResponseRepository.findReceivedRequestDates(userId, start, end);

        List<LocalDate> allDates = Stream.concat(myRequestDates.stream(), receivedRequestDates.stream())
                .distinct()
                .sorted()
                .toList();

        return teumConverter.toDateStringList(allDates);
    }

    @Override
    public List<String> getScheduledTeumsOfMonth(Long userId, String month) {
        List<ScheduleStatus> validStatuses = List.of(ScheduleStatus.ACTIVE, ScheduleStatus.COMPLETED);

        YearMonth ym = YearMonth.parse(month);
        LocalDate start = ym.atDay(1);
        LocalDate end = ym.atEndOfMonth();

        List<LocalDate> dates = scheduleRepository.findScheduledTeumsByDates(userId, validStatuses, start, end);
        return teumConverter.toDateStringList(dates);
    }


    @Override
    @Transactional(readOnly = true)
    public List<TeumResponseDto.ScheduledTeum> getScheduledTeums(Long userId, String date) {
        LocalDate targetDate = LocalDate.parse(date);

        List<Schedule> allSchedules = scheduleRepository.findByUserIdAndDateAndStatusIn(
                userId, targetDate, List.of(ScheduleStatus.ACTIVE, ScheduleStatus.COMPLETED)
        );

        List<Schedule> teumSchedules = allSchedules.stream()
                .filter(schedule -> schedule.getType() == ScheduleType.TEUM)
                .toList();

        if (teumSchedules.isEmpty()) {
            throw new GeneralException(TeumErrorStatus.TEUM_SCHEDULE_NOT_FOUND);
        }

        return teumSchedules.stream()
                .map(teumConverter::toScheduledTeumResponseDto)
                .toList();

    }

    public TeumResponseDto.ScheduledTeumDetail getScheduledTeumDetail(Long scheduleId, Long userId) {
        Schedule schedule = getScheduleOrThrow(scheduleId);
        validateScheduleAccessible(schedule, userId);

        List<Schedule> relatedSchedules = scheduleRepository.findByTeumRequestAndStatusIn(
                schedule.getTeumRequest(),
                List.of(ScheduleStatus.ACTIVE, ScheduleStatus.COMPLETED)
        );

        Map<Long, String> profileUrlByUserId = relatedSchedules.stream()
                .map(Schedule::getUser)
                .collect(Collectors.toMap(
                        User::getId,
                        this::toProfileUrl,
                        (a, b) -> a
                ));

        return teumConverter.toScheduledTeumDetailDto(schedule, relatedSchedules, profileUrlByUserId);
    }

    @Override
    @Transactional
    public TeumResponseDto.ScheduledTeumCancel cancelScheduledTeum(Long scheduleId, Long userId) {
        Schedule schedule = getScheduleOrThrow(scheduleId);
        validateScheduleOwner(schedule, userId);
        validateScheduleTypeIsTeum(schedule);

        if (schedule.getStatus() != ScheduleStatus.ACTIVE) {
            throw new GeneralException(TeumErrorStatus.TEUM_SCHEDULE_NOT_FOUND);
        }

        TeumRequest request = schedule.getTeumRequest();
        if (request == null) {
            throw new GeneralException(TeumErrorStatus.TEUM_REQUEST_NOT_FOUND);
        }

        // 본인 스케줄만 취소
        schedule.cancel();

        // 본인 응답만 LEFT로 (상대는 그대로)
        teumResponseRepository.findRequestAndReceiver(request.getId(), userId)
                .ifPresent(r -> r.changeStatus(ResponseStatus.LEFT));

        List<Long> cancelledUserIds = new ArrayList<>();
        cancelledUserIds.add(userId);

        // 같은 요청에 다른 ACTIVE 스케줄 조회
        List<Schedule> activeSchedules = scheduleRepository.findByTeumRequestAndStatusIn(
                request, List.of(ScheduleStatus.ACTIVE)
        );

        // 한 명만 남아 있다면, 그 사람 스케줄도 같이 취소 (응답 상태는 변경 없음)
        if (activeSchedules.size() == 1) {
            Schedule lastOne = activeSchedules.getFirst();
            lastOne.cancel();
            cancelledUserIds.add(lastOne.getUser().getId());
        }

        return TeumResponseDto.ScheduledTeumCancel.builder()
                .cancelledUserIds(cancelledUserIds)
                .build();
    }


    // 공통 가능한 시간대 계산
    @Override
    public TeumResponseDto.TeumAvailableTime getAvailableTime(User user, TeumRequestDto.TeumAvailableTime requestDto) {
        LocalDate date = LocalDate.parse(requestDto.getDate());
        DayOfWeek targetDay = date.getDayOfWeek();
        List<TimeSlot> scheduledSlots = new ArrayList<>();

        Set<Long> userIdSet = new HashSet<>(requestDto.getUserIds());
        userIdSet.add(user.getId()); // 요청자 포함

        for (Long userId : userIdSet) {
            User targetUser = getUserOrThrow(userId);

            // 일반 일정
            LocalDateTime startOfDay = date.atStartOfDay();
            LocalDateTime endOfDay = LocalDateTime.of(date, LocalTime.MAX);

            List<Schedule> schedules = scheduleRepository.findOverlappingSchedules(
                    userId, startOfDay, endOfDay, Arrays.asList(ScheduleStatus.ACTIVE, ScheduleStatus.COMPLETED)
            );

            schedules.stream()
                    .filter(schedule -> schedule.getRoutineStatus() != RoutineStatus.DELETED)
                    .map(schedule -> teumConverter.sliceScheduleToDate(schedule, date))
                    .filter(Objects::nonNull)
                    .forEach(scheduledSlots::add);

            // 틈 요청
            List<TeumRequest> teumRequests = teumRequestRepository.findTeumRequestsByUserAndDate(targetUser, date);
            for (TeumRequest tr : teumRequests) {
                String start = tr.getStartTime().toString();
                String end   = timeUtil.formatEndTime(timeUtil.convertEndTime(tr.getEndTime()));
                scheduledSlots.add(new TimeSlot(start, end));
            }

            // 수면 시간
            LocalTime sleep = targetUser.getSleepTime();
            LocalTime wake = targetUser.getWakeTime();
            if (sleep != null && wake != null) {
                if (sleep.isBefore(wake)) {
                    scheduledSlots.add(new TimeSlot(sleep.toString(), wake.toString()));
                } else {
                    scheduledSlots.add(new TimeSlot(sleep.toString(), "24:00"));
                    scheduledSlots.add(new TimeSlot("00:00", wake.toString()));
                }
            }

            // 반복 일정
            for (Routine routine : targetUser.getRoutines()) {
                if (routine.getWeekday().matches(targetDay)) {
                    LocalDateTime routineStart = LocalDateTime.of(date, routine.getStartTime());
                    LocalDateTime routineEnd = LocalDateTime.of(date, routine.getEndTime());

                    boolean isRoutineDeleted = scheduleRepository.existsDeletedRoutineInstance(
                            userId, date, routineStart, routineEnd
                    );

                    if (!isRoutineDeleted) {
                        scheduledSlots.add(new TimeSlot(
                                routine.getStartTime().toString(),
                                timeUtil.formatEndTime(timeUtil.convertEndTime(routine.getEndTime()))
                        ));
                    }
                }
            }
        }

        // 병합 → 반전
        List<TimeSlot> mergedBusy = teumConverter.mergeScheduledTimeSlots(scheduledSlots);
        List<TimeSlot> availableTime = teumConverter.invertScheduledToAvailable(mergedBusy);

        // 리스트 복사 후 정렬
        List<TimeSlot> sortedAvailable = new ArrayList<>(availableTime);
        sortedAvailable.sort(Comparator.comparing(slot -> timeUtil.parseTimeForSort(slot.getStart())));

        return new TeumResponseDto.TeumAvailableTime(date.toString(), sortedAvailable);
    }

    private String formatEnd(LocalTime t) {
        return t.equals(LocalTime.MIDNIGHT) ? "24:00" : t.format(DateTimeFormatter.ofPattern("HH:mm"));
    }


    @Override
    @Transactional(readOnly = true)
    public TeumResponseDto.SharedTeumTime getSharedTeumStats(Long loginUserId, Long targetUserId) {
        validateUserExists(targetUserId);
        validateNotSelf(loginUserId, targetUserId);

        List<Schedule> myTeumSchedules = scheduleRepository.findMySharedTeumSchedules(
                loginUserId, targetUserId, ScheduleType.TEUM, ScheduleStatus.COMPLETED
        );

        long totalMinutes = myTeumSchedules.stream()
                .mapToLong(s -> Duration.between(s.getStartTime(), s.getEndTime()).toMinutes())
                .sum();

        return TeumConverter.toSharedTeumTimeDto(totalMinutes);
    }

    @Override
    @Transactional(readOnly = true)
    public PagingResponseDto<TeumResponseDto.SharedTeumList> getSharedTeums(Long loginUserId, Long targetUserId, int page, int size) {
        validateNotSelf(loginUserId, targetUserId);
        validateUserExists(targetUserId);

        // 정렬: date desc, startTime desc
        Pageable pageable = PageRequest.of(
                Math.max(page - 1, 0),
                size,
                Sort.by(
                        Sort.Order.desc("date"),
                        Sort.Order.desc("startTime")
                )
        );

        // 스케줄 조회
        Slice<Schedule> slice = scheduleRepository.findSharedTeumSchedulesPaged(
                loginUserId,
                targetUserId,
                ScheduleType.TEUM,
                ScheduleStatus.COMPLETED,
                pageable
        );

        List<TeumResponseDto.SharedTeumList> content = slice.getContent().stream()
                .map(s -> {
                    User sender = s.getTeumRequest().getUser();
                    String url = toProfileUrl(sender);
                    return teumConverter.toSharedTeumListDto(s, loginUserId, url);
                })
                .toList();

        return new PagingResponseDto<>(content, slice.hasNext());
    }


    @Override
    public List<TeumResponseDto.TeumRequestDetail> getTeumRequestsByDate(Long userId, String date) {
        LocalDate parsedDate = LocalDate.parse(date);
        List<TeumRequest> allRequests = teumRequestRepository.findByDate(parsedDate);

        // 사용자가 요청자이거나 응답자인 요청만 필터링
        return allRequests.stream()
                .filter(req -> isParticipant(req, userId))
                .map(req -> {
                    // 재요청 여부 판단
                    boolean isResend = req.getParentRequest() != null;

                    // 약속 취소 여부 판단
                    boolean isCancelled = isTeumCancelled(req);

                    // 취소 가능 여부 계산 로직
                    boolean isCancellable = checkIfCancellable(req, userId);

                    // 요청자 + 응답자 전원의 URL 맵 구성
                    Map<Long, String> urlMap = new HashMap<>();
                    User requester = req.getUser();
                    urlMap.put(requester.getId(), toProfileUrl(requester));

                    req.getTeumResponses().forEach(r -> {
                        User recv = r.getReceiverUser();
                        urlMap.putIfAbsent(recv.getId(), toProfileUrl(recv));
                    });

                    // DTO로 변환
                    return teumConverter.toTeumRequestResponseDto(req, isCancelled, isResend, isCancellable, urlMap);
                })
                .toList();
    }

    @Override
    @Transactional
    public Long cancelTeumRequest(Long requestId, Long userId) {
        TeumRequest request = findActiveRequestOrThrow(requestId);

        // 요청자 본인만 취소 가능
        if (!request.getUser().getId().equals(userId)) {
            throw new GeneralException(TeumErrorStatus.USER_NOT_ELIGIBLE);
        }

        // 이미 종료되었거나 취소된 요청은 중복 취소 불가
        if (request.getStatus() != RequestStatus.ACTIVE) {
            throw new GeneralException(TeumErrorStatus.REQUEST_ALREADY_CLOSED);
        }

        // 요청 상태를 CANCELED로 변경
        request.markAsCanceled();

        // 연결된 응답을 모두 비활성화 처리 (응답 불가하도록)
        for (TeumResponse response : request.getTeumResponses()) {
            if (response.getStatus() == ResponseStatus.PENDING) {
                response.changeStatus(ResponseStatus.CANCELED);
            }
        }

        return requestId;
    }

    @Override
    @Transactional(readOnly = true)
    public TeumResponseDto.ConflictingScheduleResponse checkConflictingSchedules(Long userId, TeumRequestDto.ConflictCheckRequest request) {
        validateUserExists(userId);
        validateTimeOrder(request.getStartTime(), request.getEndTime());

        LocalDateTime checkStart = request.getDate().atTime(request.getStartTime());

        // 종료 시간이 00:00이면 다음 날 00:00으로 설정
        LocalDateTime checkEnd;
        if (request.getEndTime().equals(LocalTime.MIDNIGHT)) {
            checkEnd = request.getDate().plusDays(1).atStartOfDay();
        } else {
            checkEnd = request.getDate().atTime(request.getEndTime());
        }

        // Repository 조회 (기존 유지)
        List<Schedule> conflicts = scheduleRepository.findConflictingSchedules(
                userId,
                request.getDate(),
                checkStart,
                checkEnd,
                ScheduleStatus.ACTIVE,
                RoutineStatus.DELETED
        );

        return teumConverter.toConflictingScheduleResponse(conflicts);
    }

    @Override
    @Transactional(readOnly = true)
    public TeumResponseDto.ConflictingRequestResponse checkConflictingRequests(Long userId, TeumRequestDto.ConflictCheckRequest request) {
        validateUserExists(userId);

        // 시간 순서 검증
        validateTimeOrder(request.getStartTime(), request.getEndTime());

        // 겹치는 요청 조회
        List<TeumRequest> conflicts = teumRequestRepository.findConflictingRequests(
                userId,
                request.getDate(),
                request.getStartTime(),
                request.getEndTime(), // 00:00 그대로 전달
                RequestStatus.ACTIVE,
                LocalTime.MIDNIGHT
        );

        Map<Long, String> profileUrlMap = conflicts.stream()
                .flatMap(req -> req.getTeumResponses().stream()) // 모든 응답 스트림으로 평탄화
                .map(TeumResponse::getReceiverUser)
                .filter(Objects::nonNull)
                .collect(Collectors.toMap(
                        User::getId,
                        this::toProfileUrl,
                        (existing, replacement) -> existing
                ));

        return teumConverter.toConflictingRequestResponse(conflicts, profileUrlMap);
    }


    // 사용자가 해당 요청의 요청자 또는 응답자인지 여부
    private boolean isParticipant(TeumRequest req, Long userId) {
        return req.getUser().getId().equals(userId) ||
                req.getTeumResponses().stream()
                        .anyMatch(resp -> resp.getReceiverUser().getId().equals(userId));
    }

    // 약속 취소 여부 판단 로직
    private boolean isTeumCancelled(TeumRequest request) {
        // 미응답자가 없는지 여부
        boolean hasNoPending = request.getTeumResponses().stream()
                .noneMatch(r -> r.getStatus() == ResponseStatus.PENDING);

        // 파생된 스케줄이 모두 취소되었는지 여부
        boolean allSchedulesCancelled = request.getSchedules().stream()
                .allMatch(s -> s.getStatus() == ScheduleStatus.CANCELLED);

        // 파생된 스케줄이 없는 경우
        boolean hasNoSchedules = request.getSchedules().isEmpty();

        // 응답자가 모두 거절 또는 취소한 경우
        boolean allResponsesCancelled = request.getTeumResponses().stream()
                .allMatch(r -> r.getStatus() == ResponseStatus.REJECTED || r.getStatus() == ResponseStatus.LEFT ||
                        r.getStatus() == ResponseStatus.CANCELED);

        // 최종 취소 판단 조건
        return hasNoPending && (allSchedulesCancelled || (hasNoSchedules && allResponsesCancelled));
    }


    /**
     * 주어진 ID에 해당하는 User를 조회합니다.
     * - User 객체 자체가 필요한 경우에 사용합니다.
     * - 존재하지 않으면 예외를 던집니다.
     */
    private User getUserOrThrow(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new GeneralException(TeumErrorStatus.USER_NOT_ELIGIBLE));
    }

    /**
     * 주어진 ID에 해당하는 User가 존재하는지만 확인합니다.
     * - 객체 자체가 필요하지 않고, 존재 여부만 확인할 때 사용합니다.
     * - 존재하지 않으면 예외를 던집니다.
     */
    private void validateUserExists(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new GlobalHandler(UserErrorStatus.USER_NOT_FOUND);
        }
    }

    /**
     * 사용자가 INACTIVE인 경우 USER_NOT_FOUND 에러를 발생시킵니다.
     */
    private void validActiveUser(User user) {
        if(user.getStatus() == UserStatus.INACTIVE){
            throw new GlobalHandler(UserErrorStatus.USER_DELETED);
        }
    }

    private void validateNotSelf(Long loginUserId, Long targetUserId) {
        if (loginUserId.equals(targetUserId)) {
            throw new GlobalHandler(TeumErrorStatus.CANNOT_VIEW_SELF);
        }
    }

    private void validateScheduleAccessible(Schedule schedule, Long userId) {
        validateScheduleOwner(schedule, userId);
        validateScheduleTypeIsTeum(schedule);
        validateScheduleStatusValid(schedule);
    }

    private void validateScheduleOwner(Schedule schedule, Long userId) {
        if (!schedule.getUser().getId().equals(userId)) {
            throw new GlobalHandler(TeumErrorStatus.USER_NOT_ELIGIBLE);
        }
    }

    private void validateScheduleTypeIsTeum(Schedule schedule) {
        if (schedule.getType() != ScheduleType.TEUM) {
            throw new GlobalHandler(TeumErrorStatus.TEUM_SCHEDULE_NOT_FOUND);
        }
    }

    private void validateScheduleStatusValid(Schedule schedule) {
        if (!(schedule.getStatus() == ScheduleStatus.ACTIVE || schedule.getStatus() == ScheduleStatus.COMPLETED)) {
            throw new GlobalHandler(TeumErrorStatus.TEUM_SCHEDULE_NOT_FOUND);
        }
    }

    private Schedule getScheduleOrThrow(Long scheduleId) {
        return scheduleRepository.findById(scheduleId)
            .orElseThrow(() -> new GlobalHandler(TeumErrorStatus.TEUM_SCHEDULE_NOT_FOUND));
    }

    private TeumResponse getResponseOrThrow(Long responseId) {
        return teumResponseRepository.findById(responseId)
                .orElseThrow(() -> new GeneralException(TeumErrorStatus.TEUM_RESPONSE_NOT_FOUND));
    }

    private TeumRequest findActiveRequestOrThrow(Long requestId) {
        TeumRequest request = teumRequestRepository.findById(requestId)
                .orElseThrow(() -> new GeneralException(TeumErrorStatus.TEUM_REQUEST_NOT_FOUND));
        if (request.getStatus() != RequestStatus.ACTIVE) {
            throw new GeneralException(TeumErrorStatus.REQUEST_ALREADY_CLOSED);
        }
        return request;
    }

    private void validateReceiver(TeumResponse response, Long userId) {
        if (!response.getReceiverUser().getId().equals(userId)) {
            throw new GeneralException(TeumErrorStatus.USER_NOT_ELIGIBLE);
        }
    }

    private void validateResendableRequest(TeumRequest request) {
        if (request.getParentRequest() != null) {
            throw new GeneralException(TeumErrorStatus.REQUEST_ALREADY_RESENT);
        }
        if (request.getTeumResponses().size() != 1) {
            throw new GeneralException(TeumErrorStatus.REQUEST_NOT_ONE_TO_ONE);
        }
    }

    private void validateResender(TeumRequest request, Long senderUserId) {
        User resender = getUserOrThrow(senderUserId);
        User actualReceiver = request.getTeumResponses().getFirst().getReceiverUser();
        if (!resender.getId().equals(actualReceiver.getId())) {
            throw new GeneralException(TeumErrorStatus.USER_NOT_ELIGIBLE);
        }
    }

    private void validateTimeOrder(String startTime, String endTime) {
        LocalTime start = LocalTime.parse(startTime);
        LocalTime end = endTime.equals("00:00") ? LocalTime.MAX : LocalTime.parse(endTime);

        if (!start.isBefore(end)) {
            throw new GeneralException(TeumErrorStatus.INVALID_TEUM_TIME);
        }
    }

    private void validateTimeOrder(LocalTime startTime, LocalTime endTime) {
        LocalTime effectiveEnd = endTime.equals(LocalTime.MIDNIGHT) ? LocalTime.MAX : endTime;

        if (!startTime.isBefore(effectiveEnd)) {
            throw new GeneralException(TeumErrorStatus.INVALID_TEUM_TIME);
        }
    }

    // 시작 시간이 현재 시각보다 과거인지 검증
    private void validateStartTimeNotPast(LocalDate date, LocalTime startTime) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime requestStartDateTime = LocalDateTime.of(date, startTime);

        if (requestStartDateTime.isBefore(now)) {
            throw new GeneralException(TeumErrorStatus.INVALID_TEUM_TIME);
        }
    }

    // 차단 관계 확인
    private void validateBlockRelationship(User user1, User user2) {
        if (blockRepository.existsByBlockerAndBlocked(user1, user2) ||
                blockRepository.existsByBlockerAndBlocked(user2, user1)) {
            throw new GeneralException(FriendErrorStatus.BLOCK_ACTION_FORBIDDEN);
        }
    }

    // 취소 가능 여부 판단 헬퍼 메서드
    private boolean checkIfCancellable(TeumRequest request, Long userId) {
        // 본인이 요청자인지 확인
        if (!request.getUser().getId().equals(userId)) {
            return false;
        }

        // 시작 시간이 현재 시각보다 미래인지 확인
        LocalDateTime startDateTime = LocalDateTime.of(request.getDate(), request.getStartTime());
        if (startDateTime.isBefore(LocalDateTime.now())) {
            return false;
        }

        // 아무도 응답하지 않았는지 확인 (모든 응답이 PENDING 상태여야 함)
        return request.getTeumResponses().stream()
                .allMatch(response -> response.getStatus() == ResponseStatus.PENDING);
    }

}
