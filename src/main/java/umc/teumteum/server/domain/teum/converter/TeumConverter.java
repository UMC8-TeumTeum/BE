package umc.teumteum.server.domain.teum.converter;

import java.time.Duration;

import umc.teumteum.server.domain.home.entity.Schedule;
import umc.teumteum.server.domain.home.entity.enums.ScheduleStatus;
import umc.teumteum.server.domain.home.entity.enums.ScheduleType;
import umc.teumteum.server.domain.teum.dto.common.TimeSlot;
import umc.teumteum.server.domain.teum.dto.teum.TeumReceivedResponseDto;
import umc.teumteum.server.domain.teum.dto.teum.TeumRequestDto;
import umc.teumteum.server.domain.teum.dto.teum.TeumResendRequestDto;
import umc.teumteum.server.domain.teum.dto.teum.TeumStatusUpdateResponseDto;
import umc.teumteum.server.domain.teum.entity.TeumRequest;
import umc.teumteum.server.domain.teum.entity.TeumResponse;
import umc.teumteum.server.domain.teum.entity.enums.ResponseStatus;
import umc.teumteum.server.domain.teum.exception.status.TeumErrorStatus;
import umc.teumteum.server.domain.user.entity.User;
import umc.teumteum.server.global.exception.GeneralException;
import umc.teumteum.server.domain.teum.dto.common.ParticipantDto;
import umc.teumteum.server.global.util.S3Util;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.function.Function;

public class TeumConverter {

    public static TeumRequest toTeumRequest(TeumRequestDto dto, User sender) {
        return TeumRequest.builder()
                .title(dto.getTitle())
                .description(dto.getDescription())
                .date(LocalDate.parse(dto.getDate()))
                .startTime(LocalTime.parse(dto.getStartTime()))
                .endTime(LocalTime.parse(dto.getEndTime()))
                .graphicId(dto.getGraphicId())
                .user(sender)
                .build();
    }

    public static List<TeumResponse> toTeumResponses(
            List<Long> receiverUserIds, Long senderId,
            TeumRequest request, Function<Long, User> userFetcher
    ) {
        return receiverUserIds.stream()
                .distinct()
                .map(receiverId -> {
                    if (receiverId.equals(senderId)) {
                        throw new GeneralException(TeumErrorStatus.CANNOT_REQUEST_SELF);
                    }
                    User receiver = userFetcher.apply(receiverId);
                    return TeumResponse.builder()
                            .teumRequest(request)
                            .receiverUser(receiver)
                            .status(ResponseStatus.PENDING)
                            .readAt(null)
                            .message("")
                            .build();
                })
                .toList();
    }

    public static TeumReceivedResponseDto toReceivedResponseDto(TeumResponse response, S3Util s3Util) {
        TeumRequest request = response.getTeumRequest();
        User sender = request.getUser();

        return TeumReceivedResponseDto.builder()
                .responseId(response.getId())
                .requestId(request.getId())
                .title(request.getTitle())
                .description(request.getDescription())
                .graphicId(request.getGraphicId())
                .isRead(response.getReadAt() != null)
                .receiverCount(request.getTeumResponses().size())
                .senderUser(ParticipantDto.builder()
                        .userId(sender.getId())
                        .nickname(sender.getNickname())
                        .profileImageUrl(s3Util.toPresignedUrl(sender.getProfileImageKey(),
                            Duration.ofMinutes(30)))
                        .build())
                .date(request.getDate().toString())
                .timeSlot(TimeSlot.builder()
                        .start(request.getStartTime().toString())
                        .end(request.getEndTime().toString())
                        .build())
                .build();
    }


    public static List<TeumReceivedResponseDto> toReceivedResponseDtoList(List<TeumResponse> responses, S3Util s3Util) {
        return responses.stream()
                .map(response -> toReceivedResponseDto(response, s3Util))
                .toList();
    }

    public static TeumRequest toResendTeumRequest(TeumRequest parent, TeumResendRequestDto dto, User resender) {
        return TeumRequest.builder()
                .title(parent.getTitle())
                .description(parent.getDescription())
                .date(parent.getDate())
                .startTime(LocalTime.parse(dto.getStartTime()))
                .endTime(LocalTime.parse(dto.getEndTime()))
                .graphicId(parent.getGraphicId())
                .user(resender)
                .parentRequest(parent)
                .build();
    }

    public static TeumResponse toResendTeumResponse(TeumRequest request, User newReceiver) {
        return TeumResponse.builder()
                .teumRequest(request)
                .receiverUser(newReceiver)
                .status(ResponseStatus.PENDING)
                .message("")
                .readAt(null)
                .build();
    }

    public static Schedule toScheduleFromTeumRequest(TeumRequest request, User receiver) {
        LocalDateTime start = LocalDateTime.of(request.getDate(), request.getStartTime());
        LocalDateTime end = LocalDateTime.of(request.getDate(), request.getEndTime());

        return Schedule.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .type(ScheduleType.TEUM)
                .date(request.getDate())
                .startTime(start)
                .endTime(end)
                .user(receiver)
                .isPublic(false)
                .includeTeum(false)
                .teumRequest(request)
                .status(ScheduleStatus.ACTIVE)
                .build();
    }

    public static TeumStatusUpdateResponseDto toStatusUpdateResponseDto(ResponseStatus status, boolean isAccepted, Long teumId) {
        return TeumStatusUpdateResponseDto.builder()
                .status(status)
                .teumCreated(isAccepted)
                .teumId(teumId)
                .build();
    }

    public static TimeSlot fromSchedule(Schedule schedule) {
        return new TimeSlot(
                schedule.getStartTime().toLocalTime().toString(),
                schedule.getEndTime().toLocalTime().toString()
        );
    }

    public static List<TimeSlot> mergeScheduledTimeSlots(List<TimeSlot> scheduledSlots) {
        if (scheduledSlots.isEmpty()) return Collections.emptyList();

        List<TimeSlot> sorted = new ArrayList<>(scheduledSlots);
        sorted.sort(Comparator.comparing(slot -> LocalTime.parse(slot.getStart())));

        List<TimeSlot> merged = new ArrayList<>();
        TimeSlot current = sorted.get(0);

        for (int i = 1; i < sorted.size(); i++) {
            TimeSlot next = sorted.get(i);
            LocalTime currEnd = LocalTime.parse(current.getEnd());
            LocalTime nextStart = LocalTime.parse(next.getStart());

            if (!currEnd.isBefore(nextStart)) {
                current = new TimeSlot(
                        current.getStart(),
                        LocalTime.parse(current.getEnd()).isAfter(LocalTime.parse(next.getEnd()))
                                ? current.getEnd() : next.getEnd()
                );
            } else {
                merged.add(current);
                current = next;
            }
        }
        merged.add(current);
        return merged;
    }

    public static List<TimeSlot> invertScheduledToAvailable(List<TimeSlot> scheduledSlots) {
        List<TimeSlot> available = new ArrayList<>();
        LocalTime startOfDay = LocalTime.of(0, 0);
        LocalTime endOfDay = LocalTime.of(23, 59);

        for (TimeSlot scheduled : scheduledSlots) {
            LocalTime scheduledStart = LocalTime.parse(scheduled.getStart());
            LocalTime scheduledEnd = LocalTime.parse(scheduled.getEnd());

            if (startOfDay.isBefore(scheduledStart)) {
                available.add(new TimeSlot(startOfDay.toString(), scheduledStart.toString()));
            }
            startOfDay = scheduledEnd;
        }

        if (startOfDay.isBefore(endOfDay)) {
            available.add(new TimeSlot(startOfDay.toString(), endOfDay.toString()));
        }

        return available;
    }


}
