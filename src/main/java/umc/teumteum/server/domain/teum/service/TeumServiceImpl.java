package umc.teumteum.server.domain.teum.service;

import lombok.RequiredArgsConstructor;
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

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static umc.teumteum.server.domain.teum.exception.status.TeumErrorStatus.INVALID_PARENT_REQUEST;

@Service
@RequiredArgsConstructor
public class TeumServiceImpl implements TeumService {

    private final S3Util s3Util;
    private final UserRepository userRepository;
    private final TeumRequestRepository teumRequestRepository;
    private final TeumResponseRepository teumResponseRepository;
    private final ScheduleRepository scheduleRepository;

    @Override
    @Transactional
    public Long createRequest(TeumRequestDto dto, User user) {
        TeumRequest request = TeumConverter.toTeumRequest(dto, user);

        List<TeumResponse> responses = TeumConverter.toTeumResponses(
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
        TeumRequest parent = findActiveRequestOrThrow(parentRequestId);

        validateResendableRequest(parent);
        validateResender(parent, user.getId());
        validateTimeOrder(dto.getStartTime(), dto.getEndTime());

        TeumRequest newRequest = TeumConverter.toResendTeumRequest(parent, dto, user); // ✅ User 객체 직접 전달

        TeumResponse newResponse = TeumConverter.toResendTeumResponse(newRequest, parent.getUser());
        newRequest.getTeumResponses().add(newResponse);

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
                .map(response -> TeumConverter.toReceivedResponseDto(response, s3Util))
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
        TeumResponse response = getResponseOrThrow(responseId);
        validateReceiver(response, userId);

        if (response.getStatus() != ResponseStatus.PENDING) {
            throw new GeneralException(TeumErrorStatus.REQUEST_ALREADY_CLOSED);
        }

        ResponseStatus newStatus;
        try {
            newStatus = ResponseStatus.valueOf(requestDto.getStatus().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new GeneralException(TeumErrorStatus.INVALID_RESPONSE_STATUS);
        }

        response.changeStatus(newStatus);

        boolean isAccepted = newStatus == ResponseStatus.ACCEPTED;
        Long teumId = null;

        if (isAccepted) {
            TeumRequest request = response.getTeumRequest();

            // 이미 해당 요청에 대해 생성된 스케줄이 있는지 확인
            boolean hasExistingSchedules = !request.getSchedules().isEmpty();

            User receiver = response.getReceiverUser();
            Schedule receiverSchedule = TeumConverter.toScheduleFromTeumRequest(request, receiver);
            scheduleRepository.save(receiverSchedule);

            // 스케줄이 처음 생성되는 경우에만 요청자도 생성
            if (!hasExistingSchedules) {
                User requester = request.getUser();
                Schedule requesterSchedule = TeumConverter.toScheduleFromTeumRequest(request, requester);
                scheduleRepository.save(requesterSchedule);
            }

            // 수신자 본인 스케줄 ID 반환
            teumId = receiverSchedule.getId();
        }


        return TeumConverter.toStatusUpdateResponseDto(newStatus, isAccepted, teumId);
    }


    @Override
    public List<String> getScheduledTeumsOfMonth(Long userId, String month) {
        List<ScheduleStatus> validStatuses = List.of(ScheduleStatus.ACTIVE, ScheduleStatus.COMPLETED);
        List<LocalDate> dates = scheduleRepository.findScheduledTeumsByMonth(userId, validStatuses, month);
        return TeumConverter.toDateStringList(dates);
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
                .map(TeumConverter::toScheduledTeumResponseDto)
                .toList();

    }

    public ScheduledTeumDetailResponseDto getScheduledTeumDetail(Long scheduleId, Long userId) {
        Schedule schedule = getScheduleOrThrow(scheduleId);
        validateScheduleAccessible(schedule, userId);

        List<Schedule> relatedSchedules = scheduleRepository.findByTeumRequestAndStatusIn(
                schedule.getTeumRequest(),
                List.of(ScheduleStatus.ACTIVE, ScheduleStatus.COMPLETED)
        );

        return TeumConverter.toScheduledTeumDetailDto(schedule, relatedSchedules, s3Util);
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

        // 요청자 ID 자동 포함
        Set<Long> userIdSet = new HashSet<>(requestDto.getUserIds());
        userIdSet.add(user.getId());

        for (Long userId : userIdSet) {
            User targetUser = getUserOrThrow(userId);

            // 일반 일정
            List<Schedule> schedules = scheduleRepository.findByUserIdAndDateAndStatus(
                    userId, date, ScheduleStatus.ACTIVE
            );
            schedules.stream()
                    .filter(schedule -> !Boolean.TRUE.equals(schedule.getIsDeleted()))
                    .map(TeumConverter::fromSchedule)
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

        List<TimeSlot> merged = TeumConverter.mergeScheduledTimeSlots(scheduledSlots);
        List<TimeSlot> available = TeumConverter.invertScheduledToAvailable(merged);

        return new AvailableTimeResponseDto(date.toString(), available);
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
        LocalTime end = LocalTime.parse(endTime);
        if (!start.isBefore(end)) {
            throw new GeneralException(TeumErrorStatus.INVALID_TEUM_TIME);
        }
    }

}
