package umc.teumteum.server.global.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import umc.teumteum.server.domain.user.dto.OnboardingRequestDto;
import umc.teumteum.server.domain.user.entity.Routine;

import java.time.LocalTime;

@Getter
@AllArgsConstructor
public class TimeRange {
    private final LocalTime startTime;
    private final LocalTime endTime;

    // RoutineDTO에서 TimeRange로 변환
    public static TimeRange from(OnboardingRequestDto.RoutineDTO routine) {
        return new TimeRange(routine.getStartTime(), routine.getEndTime());
    }

    // Routine에서 TimeRange로 변환
    public static TimeRange from(Routine routine) {
        return new TimeRange(routine.getStartTime(), routine.getEndTime());
    }

    // [시작&종료] 일정을 TimeRange로 변환
    public static TimeRange of(LocalTime startTime, LocalTime endTime) {
        return new TimeRange(startTime, endTime);
    }
}
