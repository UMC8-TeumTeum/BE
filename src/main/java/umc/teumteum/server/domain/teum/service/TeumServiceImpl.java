package umc.teumteum.server.domain.teum.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import umc.teumteum.server.domain.home.entity.Schedule;
import umc.teumteum.server.domain.home.entity.enums.ScheduleStatus;
import umc.teumteum.server.domain.home.repository.ScheduleRepository;
import umc.teumteum.server.domain.teum.converter.TeumConverter;
import umc.teumteum.server.domain.teum.dto.availability.AvailableTimeRequestDto;
import umc.teumteum.server.domain.teum.dto.availability.AvailableTimeResponseDto;
import umc.teumteum.server.domain.teum.dto.common.TimeSlot;
import umc.teumteum.server.domain.teum.dto.schedule.ScheduledTeumDetailResponseDto;
import umc.teumteum.server.domain.teum.dto.schedule.ScheduledTeumExitResponseDto;
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
import umc.teumteum.server.domain.user.repository.UserRepository;
import umc.teumteum.server.global.exception.GeneralException;
import umc.teumteum.server.global.util.S3Util;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

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
    public Long createRequest(TeumRequestDto dto) {
        User sender = getUserOrThrow(dto.getSenderUserId());

        TeumRequest request = TeumConverter.toTeumRequest(dto, sender);

        List<TeumResponse> responses = TeumConverter.toTeumResponses(
                dto.getReceiverUserIds(),
                sender.getId(),
                request,
                this::getUserOrThrow
        );

        request.getTeumResponses().addAll(responses);
        teumRequestRepository.save(request);

        return request.getId();
    }

    @Override
    @Transactional
    public Long createResendRequest(Long parentRequestId, TeumResendRequestDto dto) {
        TeumRequest parent = findActiveRequestOrThrow(parentRequestId);

        validateResendableRequest(parent);
        validateResender(parent, dto.getSenderUserId());
        validateTimeOrder(dto.getStartTime(), dto.getEndTime());

        User resender = getUserOrThrow(dto.getSenderUserId());

        TeumRequest newRequest = TeumConverter.toResendTeumRequest(parent, dto, resender);
        TeumResponse newResponse = TeumConverter.toResendTeumResponse(newRequest, parent.getUser());
        newRequest.getTeumResponses().add(newResponse);

        TeumResponse originalResponse = parent.getTeumResponses().getFirst();
        originalResponse.changeStatus(ResponseStatus.RESEND);

        teumRequestRepository.save(newRequest);

        return newRequest.getId();
    }

    @Override
    @Transactional(readOnly = true)
    public Page<TeumReceivedResponseDto> getReceivedRequests(Long userId, Pageable pageable) {
        getUserOrThrow(userId);

        LocalDate today = LocalDate.now();
        LocalTime now = LocalTime.now();

        Page<TeumResponse> page = teumResponseRepository.findValidPendingResponses(userId, today, now, pageable);

        List<TeumReceivedResponseDto> dtoList = page.getContent().stream()
                .map(response -> TeumConverter.toReceivedResponseDto(response, s3Util))
                .toList();

        return new PageImpl<>(dtoList, pageable, page.getTotalElements());
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
            User receiver = response.getReceiverUser();

            Schedule schedule = TeumConverter.toScheduleFromTeumRequest(request, receiver);
            scheduleRepository.save(schedule);

            teumId = schedule.getId();
        }

        return TeumConverter.toStatusUpdateResponseDto(newStatus, isAccepted, teumId);

    }

    @Override
    public List<String> getScheduledTeumsOfMonth(Long userId, String month) {
        // TODO: 약속된 틈의 날짜 리스트 조회 로직 추후 구현
        return List.of();
    }

    @Override
    public List<ScheduledTeumResponseDto> getScheduledTeums(Long userId, String date) {
        // TODO: 특정 날짜의 약속된 틈 조회 로직 추후 구현
        return List.of();
    }

    @Override
    public ScheduledTeumDetailResponseDto getScheduledTeumDetail(Long teumId, Long userId) {
        // TODO: 약속된 틈 상세 조회 로직 추후 구현
        return null;
    }

    @Override
    public ScheduledTeumExitResponseDto exitScheduledTeum(Long teumId, Long userId) {
        // TODO: 약속된 틈 취소 로직 추후 구현
        return null;
    }

    @Override
    public AvailableTimeResponseDto getAvailableTime(AvailableTimeRequestDto requestDto) {
        Long requesterId = requestDto.getRequesterId();
        if (!requestDto.getUserIds().contains(requesterId)) {
            throw new GeneralException(TeumErrorStatus.USER_NOT_ELIGIBLE);
        }

        LocalDate date = LocalDate.parse(requestDto.getDate());
        List<TimeSlot> scheduledSlots = new ArrayList<>();
        DayOfWeek targetDay = date.getDayOfWeek();

        for (Long userId : requestDto.getUserIds()) {
            User user = getUserOrThrow(userId);

            // 일반 일정
            List<Schedule> schedules = scheduleRepository.findByUserIdAndDateAndStatus(
                    userId, date, ScheduleStatus.ACTIVE
            );
            schedules.stream()
                    .filter(schedule -> !Boolean.TRUE.equals(schedule.getIsDeleted()))
                    .map(TeumConverter::fromSchedule)
                    .forEach(scheduledSlots::add);

            // 수면 시간
            LocalTime sleep = user.getSleepTime();
            LocalTime wake = user.getWakeTime();
            if (sleep != null && wake != null) {
                if (sleep.isBefore(wake)) {
                    scheduledSlots.add(new TimeSlot(sleep.toString(), wake.toString()));
                } else {
                    scheduledSlots.add(new TimeSlot(sleep.toString(), "23:59"));
                    scheduledSlots.add(new TimeSlot("00:00", wake.toString()));
                }
            }

            // 반복 일정
            for (Routine routine : user.getRoutines()) {
                if (routine.getWeekday().matches(targetDay)) {
                    LocalDateTime routineStart = LocalDateTime.of(date, routine.getStartTime());
                    LocalDateTime routineEnd = LocalDateTime.of(date, routine.getEndTime());

                    boolean isRoutineDeleted = scheduleRepository.existsDeletedRoutineInstance(
                            userId, date, routineStart, routineEnd);

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

    private User getUserOrThrow(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new GeneralException(TeumErrorStatus.USER_NOT_ELIGIBLE));
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
