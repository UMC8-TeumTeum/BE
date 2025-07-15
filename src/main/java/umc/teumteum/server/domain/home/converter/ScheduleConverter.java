package umc.teumteum.server.domain.home.converter;

import org.springframework.stereotype.Component;
import umc.teumteum.server.domain.home.dto.CreateTodoRequestDTO;
import umc.teumteum.server.domain.home.entity.Schedule;
import umc.teumteum.server.domain.home.entity.ScheduleReminder;
import umc.teumteum.server.domain.home.entity.enums.ScheduleType;
import umc.teumteum.server.domain.user.entity.User;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class ScheduleConverter {

    // CreateTodoRequestDTO -> Schedule
    public Schedule toSchedule(CreateTodoRequestDTO dto) {
        return Schedule.builder()
                .title(dto.getTitle())
                .date(dto.getDate())
                .startTime(LocalDateTime.of(dto.getDate(), dto.getStartTime()))
                .endTime(LocalDateTime.of(dto.getDate(), dto.getEndTime()))
                .description(dto.getDescription())
                .isPublic(dto.getIsPublic())
                .includeTeum(dto.getIncludeTeum())
                .type(ScheduleType.TODO)
                .user(User.builder().id(1L).build()) // 임시 유저
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
}
