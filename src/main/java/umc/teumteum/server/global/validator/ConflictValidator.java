package umc.teumteum.server.global.validator;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import umc.teumteum.server.domain.home.entity.Schedule;
import umc.teumteum.server.domain.home.entity.enums.ScheduleStatus;
import umc.teumteum.server.domain.home.entity.enums.ScheduleType;
import umc.teumteum.server.domain.home.exception.status.HomeErrorStatus;
import umc.teumteum.server.domain.home.repository.ScheduleRepository;
import umc.teumteum.server.domain.user.entity.User;
import umc.teumteum.server.global.exception.GeneralException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@RequiredArgsConstructor
@Component
public class ConflictValidator {

    private final ScheduleRepository scheduleRepository;

    // 틈 관련 중복 검증 로직
    public void validateTeum(User user, LocalDate date, LocalTime startTime, LocalTime endTime) {
        LocalDateTime startDateTime = LocalDateTime.of(date, startTime);
        LocalDateTime endDateTime = LocalDateTime.of(date, endTime);

        checkWithSchedules(user, startDateTime, endDateTime);
        checkWithTeumRequests(user, date, startTime, endTime);
        checkWithRoutines(user, date, startTime, endTime);
        checkWithSleepPattern(user, startTime, endTime);
    }

    // TODO: 다른 일정에서의 중복 검증 로직 추가

    // 내부 충돌 검사 메서드들
    private void checkWithSchedules(User user, LocalDateTime startDateTime, LocalDateTime endDateTime) {
        List<ScheduleType> types = List.of(ScheduleType.TODO, ScheduleType.WISH, ScheduleType.AI);

        List<Schedule> conflicts = scheduleRepository.findConflictingSchedules(
                user,
                types,
                startDateTime,
                endDateTime
        );

        if (!conflicts.isEmpty()) {
            throw new GeneralException(HomeErrorStatus._SCHEDULE_CONFLICT);
        }
    }


    private void checkWithTeumRequests(User user, LocalDate date, LocalTime startTime, LocalTime endTime) {
        // TODO: 확정되지 않은 ACTIVE TeumRequest && 시간 겹침 검사
    }

    private void checkWithRoutines(User user, LocalDate date, LocalTime startTime, LocalTime endTime) {
        // TODO: 반복 일정 루틴 + 루틴 ID 기반 Schedule 조회 + isDeleted 여부로 충돌 판단
    }

    private void checkWithSleepPattern(User user, LocalTime startTime, LocalTime endTime) {
        // TODO: sleepTime, wakeTime과 시간 겹침 판단
    }
}