package umc.teumteum.server.domain.user.converter;

import umc.teumteum.server.domain.home.entity.Schedule;
import umc.teumteum.server.domain.home.entity.ScheduleReminder;
import umc.teumteum.server.domain.home.entity.enums.ScheduleType;
import umc.teumteum.server.domain.user.dto.OnboardingRequestDto;
import umc.teumteum.server.domain.user.dto.OnboardingResponseDto;
import umc.teumteum.server.domain.user.entity.Agreement;
import umc.teumteum.server.domain.user.entity.RemindAlarm;
import umc.teumteum.server.domain.user.entity.Routine;
import umc.teumteum.server.domain.user.entity.User;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class OnboardingConverter {

    // 온보딩 - 약관
    public static Agreement toAgreement(OnboardingRequestDto.AgreeRequest request, User user) {
        return Agreement.builder()
                .user(user)
                .thirdPartyConsent(request.getThirdPartyConsent())
                .marketingConsent(request.getMarketingConsent())
                .build()
                ;
    }


    // 온보딩 - 프리사인드 URL
    public static OnboardingResponseDto.ProfileImagePresignedUrlResponse toProfileImagePresignedUrlResponse(
            String presignedUrl,
            String fileName
    ) {
        return OnboardingResponseDto.ProfileImagePresignedUrlResponse.builder()
                .presignedUrl(presignedUrl)
                .fileName(fileName)
                .build()
                ;
    }


    // 온보딩 - 반복일정
    public static Routine toRoutine(OnboardingRequestDto.RoutineDTO request, User user) {
        return Routine.builder()
                .user(user)
                .title(request.getTitle())
                .description(request.getDescription())
                .weekday(request.getWeekday())
                .startTime(request.getStartTime())
                .endTime(request.getEndTime())
                .build()
                ;
    }

    public static List<Routine> toRoutineList(List<OnboardingRequestDto.RoutineDTO> requests, User user) {
        return requests.stream()
                .map(request -> toRoutine(request, user))
                .collect(Collectors.toList())
                ;
    }


    // 온보딩 - 반복일정 -> Schedule
    public static List<Schedule> toScheduleList(List<Routine> routines, User user, LocalDate today) {
        return routines.stream()
                .map(routine -> Schedule.builder()
                        .title(routine.getTitle())
                        .description(routine.getDescription())
                        .type(ScheduleType.ROUTINE)
                        .date(today)
                        .startTime(LocalDateTime.of(today, routine.getStartTime()))
                        .endTime(LocalDateTime.of(today, routine.getEndTime()))
                        .user(user)
                        .routine(routine)
                        .build()
                )
                .toList()
                ;
    }


    // 온보딩 - 리마인드 알림
    public static RemindAlarm toRemindAlarm(Integer minutes, User user) {
        return RemindAlarm.builder()
                .user(user)
                .minutesBefore(minutes)
                .build()
                ;
    }

    public static List<RemindAlarm> toRemindAlarmList(List<Integer> reminderMinutes, User user) {
        return reminderMinutes.stream()
                .map(minutes -> toRemindAlarm(minutes, user))
                .collect(Collectors.toList())
                ;
    }


    // 온보딩 - RemindAlarm O & Schedule O -> RemindAlarmr값으로 ScheduleReminder
    public static ScheduleReminder toScheduleReminder(Schedule schedule, Integer reminderTime) {
        return ScheduleReminder.builder()
                .schedule(schedule)
                .reminderTime(reminderTime)
                .build()
                ;
    }

    public static List<ScheduleReminder> toScheduleReminderList(List<Integer> reminderTimes, List<Schedule> schedules) {
        List<ScheduleReminder> scheduleReminders = new ArrayList<>();

        for (Schedule schedule : schedules) {
            for (Integer reminderTime : reminderTimes) {
                scheduleReminders.add(toScheduleReminder(schedule, reminderTime));
            }
        }

        return scheduleReminders;
    }
}
