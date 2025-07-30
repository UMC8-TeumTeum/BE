package umc.teumteum.server.domain.user.converter;

import umc.teumteum.server.domain.home.entity.Schedule;
import umc.teumteum.server.domain.home.entity.enums.ScheduleType;
import umc.teumteum.server.domain.user.dto.OnboardingResponseDto;
import umc.teumteum.server.domain.user.entity.Routine;
import umc.teumteum.server.domain.user.entity.User;
import umc.teumteum.server.domain.user.entity.enums.Weekday;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public class OnboardingConverter {

    // 온보딩 - 이미지 URL
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


    // 온보딩 - 반복일정 -> Schedule
    public static List<Schedule> toScheduleList(List<Routine> routines, User user, LocalDate today) {
        Weekday todayWeekday = Weekday.from(today.getDayOfWeek());

        return routines.stream()
                .filter(routine -> routine.getWeekday() == todayWeekday)
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
                .toList();
    }
}
