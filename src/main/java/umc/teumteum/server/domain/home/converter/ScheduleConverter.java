package umc.teumteum.server.domain.home.converter;

import java.time.Duration;
import org.springframework.stereotype.Component;
import umc.teumteum.server.domain.home.dto.request.TodoRequestDto;
import umc.teumteum.server.domain.home.dto.request.WishAssignRequestDto;
import umc.teumteum.server.domain.home.dto.response.HomeResponseDto;
import umc.teumteum.server.domain.home.dto.response.TodoInfoResponseDto;
import umc.teumteum.server.domain.home.entity.Schedule;
import umc.teumteum.server.domain.home.entity.ScheduleReminder;
import umc.teumteum.server.domain.home.entity.Wish;
import umc.teumteum.server.domain.home.entity.enums.ScheduleType;
import umc.teumteum.server.domain.user.entity.Routine;
import umc.teumteum.server.domain.user.entity.User;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class ScheduleConverter {

    // TodoRequestDTO -> Schedule
    public Schedule toSchedule(TodoRequestDto dto, User user) {
        return Schedule.builder()
                .title(dto.getTitle())
                .date(dto.getStartTime().toLocalDate())
                .startTime(dto.getStartTime())
                .endTime(dto.getEndTime())
                .description(dto.getDescription())
                .isPublic(dto.getIsPublic())
                .includeTeum(dto.getIncludeTeum())
                .type(ScheduleType.TODO)
                .user(user)
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
    public TodoInfoResponseDto toTodoInfoResponse(Schedule schedule, List<ScheduleReminder> reminders, List<String> profileUrls) {

        return TodoInfoResponseDto.builder()
                .type(schedule.getType())
                .title(schedule.getTitle())
                .startTime(schedule.getStartTime())
                .endTime(schedule.getEndTime())
                .description(schedule.getDescription())
                .isPublic(schedule.getIsPublic())
                .includeTeum(schedule.getIncludeTeum())
                .remindAlarm(reminders.stream()
                        .map(ScheduleReminder::getReminderTime)
                        .toList())
                .profileUrl(profileUrls)
                .build();
    }

    // Routine -> TodoInfoResponseDTO (미래의 반복일정 조회)
    public TodoInfoResponseDto toVirtualRoutineInfo(Routine routine,LocalDate date, List<String> profileUrls) {
        return TodoInfoResponseDto.builder()
                .type(ScheduleType.ROUTINE)
                .title(routine.getTitle())
                .description(routine.getDescription())
                .startTime(date.atTime(routine.getStartTime()))
                .endTime(date.atTime(routine.getEndTime()))
                .isPublic(false)
                .includeTeum(false)
                .remindAlarm(List.of())
                .profileUrl(profileUrls)
                .build();
    }

    // Wish -> Schedule entity
    public Schedule toScheduleFromWish(Wish wish, WishAssignRequestDto dto) {
        return Schedule.builder()
                .user(wish.getUser())
                .title(wish.getTitle())
                .description(wish.getContent())
                .type(ScheduleType.WISH)
                .date(dto.getStartTime().toLocalDate())
                .startTime(dto.getStartTime())
                .endTime(dto.getEndTime())
                .build();
    }

    // Duration -> TeumTimeDto
    public HomeResponseDto.TeumTimeDto toTeumTimeDto(Duration duration) {
        if (duration == null) {
            return HomeResponseDto.TeumTimeDto.builder()
                .totalMinutes(0L)
                .days(0)
                .hours(0)
                .minutes(0)
                .build();
        }

        long totalMinutes = duration.toMinutes();
        int days = (int) (totalMinutes / (60 * 24));
        int hours = (int) ((totalMinutes % (60 * 24)) / 60);
        int minutes = (int) (totalMinutes % 60);

        return HomeResponseDto.TeumTimeDto.builder()
            .totalMinutes(totalMinutes)
            .days(days)
            .hours(hours)
            .minutes(minutes)
            .build();


    }

    // Schedule -> HomeResponseDto.CalendarDto
    public List<HomeResponseDto.CalendarDto> toCalendarDto(Map<LocalDate, Boolean> calendarMap, LocalDate startDate, LocalDate endDate) {
        List<HomeResponseDto.CalendarDto> result = new ArrayList<>();

        for(LocalDate date = startDate; !date.isAfter(endDate); date = date.plusDays(1)){
            result.add(HomeResponseDto.CalendarDto.builder()
                    .date(date)
                    .hasSchedule(calendarMap.getOrDefault(date,false))
                    .build());
        }

        return result;
    }

    // Schedule -> HomeResponseDto.TodolistDto
    public HomeResponseDto.TodolistDto toScheduleDto(Schedule schedule) {
        return HomeResponseDto.TodolistDto.builder()
                .id(schedule.getId())
                .title(schedule.getTitle())
                .startTime(schedule.getStartTime().toLocalTime())
                .endTime(schedule.getEndTime().toLocalTime())
                .isPublic(schedule.getIsPublic())
                .hasAlarm(schedule.getHasAlarm())
                .type(schedule.getType())
                .build();
    }

    // Routine -> HomeResponseDto.TodolistDto (미래의 일정일 경우)
    public HomeResponseDto.TodolistDto toVirtualRoutineDto(Routine routine,LocalDate date) {

        // 가상의 ID 생성
        String dateStr = String.format("%04d%02d%02d", date.getYear(), date.getMonthValue(), date.getDayOfMonth());
        String idStr = dateStr + routine.getId();
        Long virtualId = -1 * Long.parseLong(idStr);

        return HomeResponseDto.TodolistDto.builder()
                .id(virtualId)
                .title(routine.getTitle())
                .startTime(routine.getStartTime())
                .endTime(routine.getEndTime())
                .isPublic(false)
                .hasAlarm(false)
                .type(ScheduleType.ROUTINE)
                .build();

    }

}
