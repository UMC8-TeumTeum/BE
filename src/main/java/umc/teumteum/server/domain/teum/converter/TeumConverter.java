package umc.teumteum.server.domain.teum.converter;

import java.time.Duration;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import umc.teumteum.server.domain.home.entity.Schedule;
import umc.teumteum.server.domain.home.entity.enums.ScheduleType;
import umc.teumteum.server.domain.teum.dto.common.TimeSlot;
import umc.teumteum.server.domain.teum.dto.schedule.ScheduledTeumDetailResponseDto;
import umc.teumteum.server.domain.teum.dto.schedule.ScheduledTeumResponseDto;
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
import umc.teumteum.server.global.util.TimeUtil;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class TeumConverter {

    private final TimeUtil timeUtil;

    public TeumRequest toTeumRequest(TeumRequestDto dto, User sender) {
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

    public List<TeumResponse> toTeumResponses(
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
                            .build();
                })
                .toList();
    }

    public TeumReceivedResponseDto toReceivedResponseDto(TeumResponse response, S3Util s3Util) {
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
                        .profileImageUrl(s3Util.toPresignedUrl("profile/" + sender.getProfileImageName(),
                            Duration.ofMinutes(30)))
                        .build())
                .date(request.getDate().toString())
                .timeSlot(TimeSlot.builder()
                        .start(request.getStartTime().toString())
                        .end(timeUtil.parseAndFormatEndTime(request.getEndTime()))
                        .build())
                .build();
    }


    public List<TeumReceivedResponseDto> toReceivedResponseDtoList(List<TeumResponse> responses, S3Util s3Util) {
        return responses.stream()
                .map(response -> toReceivedResponseDto(response, s3Util))
                .toList();
    }

    public TeumRequest toResendTeumRequest(TeumRequest parent, TeumResendRequestDto dto, User resender) {
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

    public TeumResponse toResendTeumResponse(TeumRequest request, User newReceiver) {
        return TeumResponse.builder()
                .teumRequest(request)
                .receiverUser(newReceiver)
                .status(ResponseStatus.PENDING)
                .readAt(null)
                .build();
    }

    public Schedule toScheduleFromTeumRequest(TeumRequest request, User receiver) {
        LocalDate date = request.getDate();
        LocalTime start = request.getStartTime();
        LocalTime end = timeUtil.convertEndTime(request.getEndTime());

        LocalDateTime startDateTime = LocalDateTime.of(date, start);

        // 00:00이 들어와서 LocalTime.MAX로 바뀐 경우 → 다음 날 00:00으로 endTime을 표현
        LocalDateTime endDateTime = end.equals(LocalTime.MAX)
                ? LocalDateTime.of(date.plusDays(1), LocalTime.MIDNIGHT)
                : LocalDateTime.of(date, end);

        return Schedule.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .type(ScheduleType.TEUM)
                .date(date)
                .startTime(startDateTime)
                .endTime(endDateTime)
                .user(receiver)
                .includeTeum(true)
                .teumRequest(request)
                .build();
    }

    public TeumStatusUpdateResponseDto toStatusUpdateResponseDto(ResponseStatus status, boolean isAccepted, Long teumId) {
        return TeumStatusUpdateResponseDto.builder()
                .status(status)
                .teumCreated(isAccepted)
                .teumId(teumId)
                .build();
    }

    public TimeSlot fromSchedule(Schedule schedule) {
        return new TimeSlot(
                schedule.getStartTime().toLocalTime().toString(),
                schedule.getEndTime().toLocalTime().toString()
        );
    }

    public List<TimeSlot> mergeScheduledTimeSlots(List<TimeSlot> scheduledSlots) {
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

    public List<TimeSlot> invertScheduledToAvailable(List<TimeSlot> scheduledSlots) {
        List<TimeSlot> available = new ArrayList<>();
        LocalTime startOfDay = LocalTime.MIN;
        LocalTime endOfDay = LocalTime.MAX;

        // 먼저 scheduled를 시간 순서대로 정렬
        List<TimeSlot> sorted = new ArrayList<>(scheduledSlots);
        sorted.sort(Comparator.comparing(slot -> LocalTime.parse(slot.getStart())));

        LocalTime current = startOfDay;

        for (TimeSlot scheduled : sorted) {
            LocalTime scheduledStart = LocalTime.parse(scheduled.getStart());
            LocalTime scheduledEnd = LocalTime.parse(scheduled.getEnd());

            if (current.isBefore(scheduledStart)) {
                available.add(new TimeSlot(current.toString(), scheduledStart.toString()));
            }

            // 다음 구간 시작 위치를 scheduledEnd 기준으로 계속 갱신
            if (current.isBefore(scheduledEnd)) {
                current = scheduledEnd;
            }
        }

        if (current.isBefore(endOfDay)) {
            available.add(new TimeSlot(
                    current.format(DateTimeFormatter.ofPattern("HH:mm")),
                    timeUtil.formatEndTime(endOfDay) // 24:00 처리
            ));
        }

        return available;
    }

    public List<String> toDateStringList(List<LocalDate> dates) {
        return dates.stream()
                .map(LocalDate::toString)
                .toList();
    }

    public ScheduledTeumDetailResponseDto toScheduledTeumDetailDto(
            Schedule baseSchedule,
            List<Schedule> relatedSchedules,
            S3Util s3Util
    ) {
        return ScheduledTeumDetailResponseDto.builder()
                .teumId(baseSchedule.getTeumRequest().getId())
                .title(baseSchedule.getTitle())
                .date(baseSchedule.getDate().toString())
                .startTime(baseSchedule.getStartTime().toLocalTime().toString())
                .endTime(timeUtil.parseAndFormatEndTime(baseSchedule.getEndTime().toLocalTime()))
                .status(baseSchedule.getStatus())
                .participants(relatedSchedules.stream()
                        .map(s -> {
                            var u = s.getUser();
                            String presignedUrl = s3Util.toPresignedUrl("profile/" + u.getProfileImageName(), Duration.ofMinutes(30));
                            return new ParticipantDto(u.getId(), u.getNickname(), presignedUrl);
                        })
                        .collect(Collectors.toList()))
                .build();
    }

    public ScheduledTeumResponseDto toScheduledTeumResponseDto(Schedule schedule) {
        return ScheduledTeumResponseDto.builder()
                .teumId(schedule.getId())
                .title(schedule.getTitle())
                .date(schedule.getDate().toString())
                .time(List.of(new TimeSlot(
                        schedule.getStartTime().toLocalTime().toString(),
                        timeUtil.parseAndFormatEndTime(schedule.getEndTime().toLocalTime())
                )))
                .build();
    }


}
