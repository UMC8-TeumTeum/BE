package umc.teumteum.server.domain.teum.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import umc.teumteum.server.domain.home.entity.Schedule;
import umc.teumteum.server.domain.home.entity.enums.ScheduleStatus;
import umc.teumteum.server.domain.home.entity.enums.ScheduleType;
import umc.teumteum.server.domain.home.repository.ScheduleRepository;
import umc.teumteum.server.domain.teum.converter.TeumConverter;
import umc.teumteum.server.domain.teum.dto.availability.AvailableTimeRequestDto;
import umc.teumteum.server.domain.teum.dto.availability.AvailableTimeResponseDto;
import umc.teumteum.server.domain.teum.dto.common.TimeSlot;
import umc.teumteum.server.domain.teum.dto.schedule.ScheduledTeumCancelResponseDto;
import umc.teumteum.server.domain.teum.dto.schedule.ScheduledTeumDetailResponseDto;
import umc.teumteum.server.domain.teum.dto.schedule.ScheduledTeumResponseDto;
import umc.teumteum.server.domain.teum.dto.shared.SharedTeumResponseDto;
import umc.teumteum.server.domain.teum.dto.teum.*;
import umc.teumteum.server.domain.teum.entity.TeumRequest;
import umc.teumteum.server.domain.teum.entity.TeumResponse;
import umc.teumteum.server.domain.teum.entity.enums.RequestStatus;
import umc.teumteum.server.domain.teum.entity.enums.ResponseStatus;
import umc.teumteum.server.domain.teum.exception.status.TeumErrorStatus;
import umc.teumteum.server.domain.teum.repository.TeumRequestRepository;
import umc.teumteum.server.domain.teum.repository.TeumResponseRepository;
import umc.teumteum.server.domain.user.entity.Routine;
import umc.teumteum.server.domain.user.entity.User;
import umc.teumteum.server.domain.user.exception.status.UserErrorStatus;
import umc.teumteum.server.domain.user.repository.UserRepository;
import umc.teumteum.server.global.exception.GeneralException;
import umc.teumteum.server.global.exception.handler.GlobalHandler;
import umc.teumteum.server.global.util.S3Util;
import umc.teumteum.server.global.util.TimeUtil;
import umc.teumteum.server.global.validator.ConflictValidator;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;

import static umc.teumteum.server.domain.teum.exception.status.TeumErrorStatus.INVALID_PARENT_REQUEST;

@Slf4j
@Service
@RequiredArgsConstructor
public class TeumServiceImpl implements TeumService {

    private final S3Util s3Util;
    private final ConflictValidator conflictValidator;
    private final UserRepository userRepository;
    private final TeumRequestRepository teumRequestRepository;
    private final TeumResponseRepository teumResponseRepository;
    private final ScheduleRepository scheduleRepository;
    private final TeumConverter teumConverter;
    private final TimeUtil timeUtil;

    @Override
    @Transactional
    public Long createRequest(TeumRequestDto dto, User user) {
        // 시간 순서 검증
        validateTimeOrder(dto.getStartTime(), dto.getEndTime());

        // 날짜 및 시간 파싱
        LocalDate date = LocalDate.parse(dto.getDate());
        LocalTime startTime = LocalTime.parse(dto.getStartTime());
        LocalTime endTime = LocalTime.parse(dto.getEndTime());

        // 모든 사용자 조회 (요청자 + 수신자)
        List<User> receivers = dto.getReceiverUserIds().stream()
                .map(this::getUserOrThrow)
                .toList();
        List<User> allParticipants = new ArrayList<>(receivers);
        allParticipants.add(user);

        // Validator 호출
        conflictValidator.validateTeumForUsers(allParticipants, date, startTime, endTime);

        // 요청 객체 생성 및 저장
        TeumRequest request = teumConverter.toTeumRequest(dto, user);
        List<TeumResponse> responses = teumConverter.toTeumResponses(
                dto.getReceiverUserIds(),
                user.getId(),
                request,
                this::getUserOrThrow
        );

        request.getTeumResponses().addAll(responses);
        teumRequestRepository.save(request);

        return request.getId();
    }

    @Override
    @Transactional
    public Long createResendRequest(Long parentRequestId, TeumResendRequestDto dto, User user) {
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

        User originalSender = parent.getUser();  // 부모 요청의 작성자 → 이번 재요청의 수신자

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

        return newRequest.getId();
    }


    @Override
    @Transactional(readOnly = true)
    public Page<TeumReceivedResponseDto> getReceivedRequests(Long userId, int page, int size) {
        getUserOrThrow(userId);

        int pageIndex = Math.max(page - 1, 0);

        Sort sort = Sort.by(
                Sort.Order.asc("readAt"),
                Sort.Order.desc("createdAt")
        );

        Pageable pageable = PageRequest.of(pageIndex, size, sort);

        Page<TeumResponse> pageData = teumResponseRepository.findValidPendingResponses(
                userId, LocalDate.now(), LocalTime.now(), pageable
        );

        List<TeumReceivedResponseDto> dtoList = pageData.getContent().stream()
                .map(response -> teumConverter.toReceivedResponseDto(response, s3Util))
                .toList();

        return new PageImpl<>(dtoList, pageable, pageData.getTotalElements());
    }

    @Override
    @Transactional
    public Long updateReadStatus(Long responseId, Long userId) {
        TeumResponse response = getResponseOrThrow(responseId);
        validateReceiver(response, userId);

        response.markAsRead();
        return responseId;
    }

    @Override
    @Transactional
    public TeumStatusUpdateResponseDto updateResponseStatus(Long responseId, Long userId, TeumStatusUpdateRequestDto requestDto) {
        // 응답 조회 및 권한 검증
        TeumResponse response = getResponseOrThrow(responseId);
        validateReceiver(response, userId);

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
            TeumRequest request = response.getTeumRequest();
            User receiver = response.getReceiverUser();

            // 수락 시 응답자 개인 일정 충돌 검증
            LocalDate date = request.getDate();
            LocalTime startTime = request.getStartTime();
            LocalTime endTime = request.getEndTime();

            conflictValidator.validateTeum(receiver, date, startTime, endTime);

            // 수신자(응답자) 일정 생성
            Schedule receiverSchedule = teumConverter.toScheduleFromTeumRequest(request, receiver);
            scheduleRepository.save(receiverSchedule);

            // 요청자 본인의 스케줄이 없는 경우에만 생성
            User requester = request.getUser();
            if (!scheduleRepository.existsByTeumRequestAndUser(request, requester)) {
                Schedule requesterSchedule = teumConverter.toScheduleFromTeumRequest(request, requester);
                scheduleRepository.save(requesterSchedule);
            }

            // 수신자 본인 스케줄 ID 반환
            teumId = receiverSchedule.getId();
        }

        return teumConverter.toStatusUpdateResponseDto(newStatus, isAccepted, teumId);
    }



    @Override
    public List<String> getScheduledTeumsOfMonth(Long userId, String month) {
        List<ScheduleStatus> validStatuses = List.of(ScheduleStatus.ACTIVE, ScheduleStatus.COMPLETED);
        List<LocalDate> dates = scheduleRepository.findScheduledTeumsByMonth(userId, validStatuses, month);
        return teumConverter.toDateStringList(dates);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ScheduledTeumResponseDto> getScheduledTeums(Long userId, String date) {
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

    public ScheduledTeumDetailResponseDto getScheduledTeumDetail(Long scheduleId, Long userId) {
        Schedule schedule = getScheduleOrThrow(scheduleId);
        validateScheduleAccessible(schedule, userId);

        List<Schedule> relatedSchedules = scheduleRepository.findByTeumRequestAndStatusIn(
                schedule.getTeumRequest(),
                List.of(ScheduleStatus.ACTIVE, ScheduleStatus.COMPLETED)
        );

        return teumConverter.toScheduledTeumDetailDto(schedule, relatedSchedules, s3Util);
    }

    @Override
    @Transactional
    public ScheduledTeumCancelResponseDto cancelScheduledTeum(Long scheduleId, Long userId) {
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

        // 본인 스케줄을 CANCELLED 처리
        schedule.cancel();
        List<Long> cancelledUserIds = new ArrayList<>();
        cancelledUserIds.add(userId);

        // 해당 틈 요청과 연결된 다른 ACTIVE 스케줄이 있는지 조회
        List<Schedule> activeSchedules = scheduleRepository.findByTeumRequestAndStatusIn(
                request, List.of(ScheduleStatus.ACTIVE)
        );

        // 한 명만 남아 있다면, 그 사람 스케줄도 같이 취소
        if (activeSchedules.size() == 1) {
            Schedule lastOne = activeSchedules.getFirst();
            lastOne.cancel();
            cancelledUserIds.add(lastOne.getUser().getId());
        }

        return ScheduledTeumCancelResponseDto.builder()
                .cancelledUserIds(cancelledUserIds)
                .build();
    }

    @Override
    public AvailableTimeResponseDto getAvailableTime(User user, AvailableTimeRequestDto requestDto) {
        LocalDate date = LocalDate.parse(requestDto.getDate());
        DayOfWeek targetDay = date.getDayOfWeek();
        List<TimeSlot> scheduledSlots = new ArrayList<>();

        Set<Long> userIdSet = new HashSet<>(requestDto.getUserIds());
        userIdSet.add(user.getId()); // 요청자 포함

        for (Long userId : userIdSet) {
            User targetUser = getUserOrThrow(userId);

            // 일반 일정
            List<Schedule> schedules = scheduleRepository.findByUserIdAndDateAndStatus(
                    userId, date, ScheduleStatus.ACTIVE
            );

            schedules.stream()
                    .filter(schedule -> schedule.getStartTime().toLocalDate().isEqual(date))
                    .filter(schedule -> !Boolean.TRUE.equals(schedule.getIsDeleted()))
                    .map(teumConverter::fromSchedule)
                    .peek(slot -> log.info("Scheduled TimeSlot for user {}: {} ~ {}", userId, slot.getStart(), slot.getEnd()))
                    .forEach(scheduledSlots::add);

            // 수면 시간
            LocalTime sleep = targetUser.getSleepTime();
            LocalTime wake = targetUser.getWakeTime();
            if (sleep != null && wake != null) {
                if (sleep.isBefore(wake)) {
                    scheduledSlots.add(new TimeSlot(sleep.toString(), wake.toString()));
                } else {
                    scheduledSlots.add(new TimeSlot(sleep.toString(), "23:59"));
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
                                routine.getEndTime().toString()
                        ));
                    }
                }
            }
        }

        // 병합 → 반전 → 병합
        List<TimeSlot> mergedBusy = teumConverter.mergeScheduledTimeSlots(scheduledSlots);
        List<TimeSlot> rawAvailable = teumConverter.invertScheduledToAvailable(mergedBusy);
        List<TimeSlot> cleanAvailable = teumConverter.mergeScheduledTimeSlots(rawAvailable);

        // 리스트 복사 후 정렬 (정렬 보장)
        List<TimeSlot> sortedAvailable = new ArrayList<>(cleanAvailable);
        sortedAvailable.sort(Comparator.comparing(slot -> timeUtil.parseTimeForSort(slot.getStart())));

        sortedAvailable.forEach(slot ->
                log.info("최종 Available (정렬됨): {} ~ {}", slot.getStart(), slot.getEnd()));

        return new AvailableTimeResponseDto(date.toString(), sortedAvailable);
    }


    @Override
    public SharedTeumResponseDto getSharedTeumStats(Long userId, Long friendId) {
        // TODO : 함께한 틈 시간 조회 로직 추후 구현
        return null;
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

}
