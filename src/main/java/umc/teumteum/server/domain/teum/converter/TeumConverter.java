package umc.teumteum.server.domain.teum.converter;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import umc.teumteum.server.domain.home.entity.Schedule;
import umc.teumteum.server.domain.home.entity.enums.ScheduleType;
import umc.teumteum.server.domain.teum.dto.common.ParticipantDto;
import umc.teumteum.server.domain.teum.dto.common.TimeSlot;
import umc.teumteum.server.domain.teum.dto.schedule.ScheduledTeumDetailResponseDto;
import umc.teumteum.server.domain.teum.dto.schedule.ScheduledTeumResponseDto;
import umc.teumteum.server.domain.teum.dto.shared.SharedTeumListResponseDto;
import umc.teumteum.server.domain.teum.dto.shared.SharedTeumTimeResponseDto;
import umc.teumteum.server.domain.teum.dto.teum.*;
import umc.teumteum.server.domain.teum.entity.TeumRequest;
import umc.teumteum.server.domain.teum.entity.TeumResponse;
import umc.teumteum.server.domain.teum.entity.enums.ResponseStatus;
import umc.teumteum.server.domain.teum.exception.status.TeumErrorStatus;
import umc.teumteum.server.domain.user.entity.User;
import umc.teumteum.server.global.exception.GeneralException;
import umc.teumteum.server.global.util.S3Util;
import umc.teumteum.server.global.util.TimeUtil;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class TeumConverter {

    private final TimeUtil timeUtil;
    private final S3Util s3Util;

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

    public TeumReceivedResponseDto toReceivedResponseDto(TeumResponse response, String senderProfileImageUrl) {
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
                        .profileImageUrl(senderProfileImageUrl)
                        .build())
                .date(request.getDate().toString())
                .timeSlot(TimeSlot.builder()
                        .start(request.getStartTime().toString())
                        .end(timeUtil.parseAndFormatEndTime(request.getEndTime()))
                        .build())
                .build();
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

    // TimeSlot 변환 (Schedule → TimeSlot)
    public TimeSlot sliceScheduleToDate(Schedule schedule, LocalDate targetDate) {
        LocalDateTime start = schedule.getStartTime();
        LocalDateTime end = schedule.getEndTime();

        LocalDateTime dayStart = LocalDateTime.of(targetDate, LocalTime.MIN);
        LocalDateTime dayEnd = LocalDateTime.of(targetDate, LocalTime.MAX);

        // 잘라낸 시작/종료 시간
        LocalDateTime slicedStart = start.isBefore(dayStart) ? dayStart : start;
        LocalDateTime slicedEnd = end.isAfter(dayEnd) ? dayEnd : end;

        if (!slicedStart.isBefore(slicedEnd)) {
            return null; // 무효한 일정
        }

        String startStr = slicedStart.toLocalTime().format(DateTimeFormatter.ofPattern("HH:mm"));
        String endStr = slicedEnd.toLocalTime().equals(LocalTime.MAX) ? "24:00"
                : slicedEnd.toLocalTime().format(DateTimeFormatter.ofPattern("HH:mm"));

        return new TimeSlot(startStr, endStr);
    }

    // 바쁜 시간대 병합 (겹치거나 인접한 TimeSlot 병합)
    public List<TimeSlot> mergeScheduledTimeSlots(List<TimeSlot> slots) {
        if (slots.isEmpty()) return List.of();

        List<TimeSlot> sorted = new ArrayList<>(slots);
        sorted.sort(Comparator.comparing(slot -> timeUtil.parseTimeForCompare(slot.getStart())));

        List<TimeSlot> merged = new ArrayList<>();
        TimeSlot current = sorted.get(0);

        for (int i = 1; i < sorted.size(); i++) {
            TimeSlot next = sorted.get(i);
            LocalTime currEnd = timeUtil.parseTimeForCompare(current.getEnd());
            LocalTime nextStart = timeUtil.parseTimeForCompare(next.getStart());
            LocalTime nextEnd = timeUtil.parseTimeForCompare(next.getEnd());

            if (!currEnd.isBefore(nextStart)) {
                String newEnd = currEnd.isAfter(nextEnd) ? current.getEnd() : next.getEnd();
                current = new TimeSlot(current.getStart(), newEnd);
            } else {
                merged.add(current);
                current = next;
            }
        }
        merged.add(current);
        return merged;
    }

    // 병합된 바쁜 시간대를 반전하여 비어 있는 시간 구간(available)을 계산
    public List<TimeSlot> invertScheduledToAvailable(List<TimeSlot> scheduledSlots) {
        List<TimeSlot> available = new ArrayList<>();
        LocalTime startOfDay = LocalTime.MIN;
        LocalTime endOfDay = LocalTime.MAX;

        List<TimeSlot> sorted = new ArrayList<>(scheduledSlots);
        sorted.sort(Comparator.comparing(slot -> timeUtil.parseTimeForCompare(slot.getStart())));

        LocalTime current = startOfDay;

        for (TimeSlot scheduled : sorted) {
            LocalTime scheduledStart = timeUtil.parseTimeForCompare(scheduled.getStart());
            LocalTime scheduledEnd = timeUtil.parseTimeForCompare(scheduled.getEnd());

            // 비어 있는 구간 발견 시 추가
            if (current.isBefore(scheduledStart)) {
                available.add(new TimeSlot(
                        current.format(DateTimeFormatter.ofPattern("HH:mm")),
                        scheduledStart.format(DateTimeFormatter.ofPattern("HH:mm"))
                ));
            }

            // 다음 시작 위치 업데이트
            if (current.isBefore(scheduledEnd)) {
                current = scheduledEnd;
            }
        }

        // 하루의 끝까지 비어 있다면 마지막 구간 추가
        if (current.isBefore(endOfDay)) {
            available.add(new TimeSlot(
                    current.format(DateTimeFormatter.ofPattern("HH:mm")),
                    timeUtil.formatEndTime(endOfDay)
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
            Map<Long, String> profileUrlByUserId
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
                            return new ParticipantDto(u.getId(), u.getNickname(), profileUrlByUserId.get(u.getId()));
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

    public static SharedTeumTimeResponseDto toSharedTeumTimeDto(long totalMinutes) {
        long days = totalMinutes / (24 * 60);
        long hours = (totalMinutes % (24 * 60)) / 60;
        long minutes = totalMinutes % 60;

        return SharedTeumTimeResponseDto.builder()
                .days(days)
                .hours(hours)
                .minutes(minutes)
                .totalMinutes(totalMinutes)
                .build();
    }


    public TeumRequestResponseDto toTeumRequestResponseDto(
            TeumRequest request,
            boolean isCancelled,
            boolean isResend,
            Map<Long, String> profileUrlByUserId
    ) {
        TimeSlot timeSlot = new TimeSlot(
                request.getStartTime().toString(),
                timeUtil.parseAndFormatEndTime(request.getEndTime())
        );

        ParticipantDto requester = toParticipantDto(request.getUser(),
                profileUrlByUserId.get(request.getUser().getId()));

        List<ParticipantDto> pending = new ArrayList<>();
        List<ParticipantDto> accepted = new ArrayList<>();
        List<ParticipantDto> cancelled = new ArrayList<>();
        List<ParticipantDto> resend = new ArrayList<>();

        for (TeumResponse response : request.getTeumResponses()) {
            ParticipantDto participant = toParticipantDto(response.getReceiverUser(),
                    profileUrlByUserId.get(response.getReceiverUser().getId()));

            switch (response.getStatus()) {
                case PENDING -> pending.add(participant);
                case ACCEPTED -> accepted.add(participant);
                case REJECTED, LEFT -> cancelled.add(participant);
                case RESEND -> resend.add(participant);
            }
        }

        return TeumRequestResponseDto.builder()
                .requestId(request.getId())
                .title(request.getTitle())
                .description(request.getDescription())
                .date(request.getDate())
                .timeSlot(timeSlot)
                .requester(requester)
                .isCancelled(isCancelled)
                .isResend(isResend)
                .pending(pending)
                .accepted(accepted)
                .cancelled(cancelled)
                .resend(resend)
                .build();
    }


    public SharedTeumListResponseDto toSharedTeumListDto(Schedule schedule, Long loginUserId, String senderProfileImageUrl) {
        TeumRequest request = schedule.getTeumRequest();
        User sender = request.getUser();

        return SharedTeumListResponseDto.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .date(request.getDate().toString())
                .time(new TimeSlot(
                        schedule.getStartTime().toLocalTime().format(DateTimeFormatter.ofPattern("HH:mm")),
                        timeUtil.parseAndFormatEndTime(schedule.getEndTime().toLocalTime())
                ))
                .sender(toParticipantDto(sender, senderProfileImageUrl))
                .isSender(sender.getId().equals(loginUserId))
                .build();
    }


    private ParticipantDto toParticipantDto(User user, String profileImageUrl) {
        return ParticipantDto.builder()
                .userId(user.getId())
                .nickname(user.getNickname())
                .profileImageUrl(profileImageUrl)
                .build();
    }


}
