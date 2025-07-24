package umc.teumteum.server.domain.home.converter;

import org.springframework.stereotype.Component;
import umc.teumteum.server.domain.home.dto.request.TodoRequestDTO;
import umc.teumteum.server.domain.home.dto.request.WishAssignRequestDTO;
import umc.teumteum.server.domain.home.dto.request.WishRequestDTO;
import umc.teumteum.server.domain.home.dto.response.TodoInfoResponseDTO;
import umc.teumteum.server.domain.home.entity.Schedule;
import umc.teumteum.server.domain.home.entity.ScheduleReminder;
import umc.teumteum.server.domain.home.entity.Wish;
import umc.teumteum.server.domain.home.entity.enums.ScheduleType;
import umc.teumteum.server.domain.user.entity.User;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class ScheduleConverter {

    // TodoRequestDTO -> Schedule
    public Schedule toSchedule(TodoRequestDTO dto) {
        return Schedule.builder()
                .title(dto.getTitle())
                .date(dto.getDate())
                .startTime(LocalDateTime.of(dto.getDate(), dto.getStartTime()))
                .endTime(LocalDateTime.of(dto.getDate(), dto.getEndTime()))
                .description(dto.getDescription())
                .isPublic(dto.getIsPublic())
                .includeTeum(dto.getIncludeTeum())
                .type(ScheduleType.TODO)
                .user(User.builder().id(dto.getUserId()).build()) // 임시 유저
                .build();
    }

    //  DTO의 remindAlarm 리스트 -> ScheduleReminder
    public List<ScheduleReminder> toScheduleReminders(Schedule schedule, List<Integer> remindAlarm) {
        return remindAlarm.stream()
                .map(time -> ScheduleReminder.builder()
                        .schedule(schedule)
                        .reminderTime(time)
                        .build())
                .collect(Collectors.toList());
    }

    // Schedule -> TodoInfoResponseDTO
    public TodoInfoResponseDTO toTodoInfoResponse(Schedule schedule, List<ScheduleReminder> reminders, List<String> profileUrls) {

        return TodoInfoResponseDTO.builder()
                .type(schedule.getType())
                .title(schedule.getTitle())
                .date(schedule.getDate())
                .startTime(schedule.getStartTime().toLocalTime())
                .endTime(schedule.getEndTime().toLocalTime())
                .description(schedule.getDescription())
                .isPublic(schedule.getIsPublic())
                .includeTeum(schedule.getIncludeTeum())
                .remindAlarm(reminders.stream()
                        .map(ScheduleReminder::getReminderTime)
                        .toList())
                .profileUrl(profileUrls)
                .build();
    }

    // Wish -> Schedule entity
    public Schedule toScheduleFromWish(Wish wish, WishAssignRequestDTO dto) {
        return Schedule.builder()
                .user(wish.getUser())
                .title(wish.getTitle())
                .description(wish.getContent())
                .type(ScheduleType.WISH)
                .date(dto.getDate())
                .startTime(LocalDateTime.of(dto.getDate(), dto.getStartTime()))
                .endTime(LocalDateTime.of(dto.getDate(), dto.getEndTime()))
                .build();
    }
}
