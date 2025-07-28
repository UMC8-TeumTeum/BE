package umc.teumteum.server.global.validator;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import umc.teumteum.server.domain.home.entity.Schedule;
import umc.teumteum.server.domain.home.entity.enums.ScheduleStatus;
import umc.teumteum.server.domain.home.entity.enums.ScheduleType;
import umc.teumteum.server.domain.home.exception.status.HomeErrorStatus;
import umc.teumteum.server.domain.home.repository.ScheduleRepository;
import umc.teumteum.server.domain.teum.entity.TeumRequest;
import umc.teumteum.server.domain.teum.exception.status.TeumErrorStatus;
import umc.teumteum.server.domain.teum.repository.TeumRequestRepository;
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
    private final TeumRequestRepository teumRequestRepository;

    /**
     * 단일 사용자에 대한 Teum 시간 중복 검증 로직
     * - 사용자가 일정 생성 또는 Teum 응답을 시도할 때 호출
     * - 일정, 미확정된 틈 요청, 반복 일정, 수면 시간과 겹침 여부를 검사
     */
    public void validateTeum(User user, LocalDate date, LocalTime startTime, LocalTime endTime) {
        LocalDateTime startDateTime = LocalDateTime.of(date, startTime);
        LocalDateTime endDateTime = LocalDateTime.of(date, endTime);

        checkWithSchedules(user, startDateTime, endDateTime);
        checkWithTeumRequests(user, date, startTime, endTime);
        checkWithRoutines(user, date, startTime, endTime);
        checkWithSleepPattern(user, date, startDateTime, endDateTime);
    }

    /**
     * 다자간 Teum 요청 시 모든 참여자에 대해 중복 검사를 수행
     * - 틈 요청 및 재요청 생성 시 사용되며, 요청자 + 모든 수신자 일정과 겹침이 없어야 생성 가능
     */
    public void validateTeumForUsers(List<User> users, LocalDate date, LocalTime startTime, LocalTime endTime) {
        for (User user : users) {
            validateTeum(user, date, startTime, endTime);
        }
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
        List<TeumRequest> conflicts = teumRequestRepository.findConflictingTeumRequest(
                user,
                date,
                startTime,
                endTime
        );

        if (!conflicts.isEmpty()) {
            throw new GeneralException(TeumErrorStatus.TEUM_REQUEST_CONFLICT);
        }
    }

    private void checkWithRoutines(User user, LocalDate date, LocalTime startTime, LocalTime endTime) {
        // TODO: 반복 일정 루틴 + 루틴 ID 기반 Schedule 조회 + isDeleted 여부로 충돌 판단
    }

    private void checkWithSleepPattern(User user, LocalDate date, LocalDateTime startDateTime, LocalDateTime endDateTime) {
        LocalTime sleepTime = user.getSleepTime();
        LocalTime wakeTime = user.getWakeTime();

        LocalDateTime sleepStart = LocalDateTime.of(date, sleepTime);
        LocalDateTime sleepEnd = sleepTime.isBefore(wakeTime)
                ? LocalDateTime.of(date, wakeTime)
                : LocalDateTime.of(date.plusDays(1), wakeTime);  // 자정 넘기는 경우 다음 날로

        if (isOverlapping(startDateTime, endDateTime, sleepStart, sleepEnd)) {
            throw new GeneralException(HomeErrorStatus._SCHEDULE_CONFLICT);
        }
    }

    /**
     * 두 시간 구간이 겹치는지 여부를 반환
     * 겹침 기준: [start1, end1) 과 [start2, end2) 가 교차하는 경우
     * - start1 < end2 && end1 > start2 일 때 겹침으로 간주
     * - 끝점이 정확히 맞닿는 경우 (예: end1 == start2)는 겹치지 않는 것으로 판단
     */
    private boolean isOverlapping(LocalDateTime start1, LocalDateTime end1, LocalDateTime start2, LocalDateTime end2) {
        return start1.isBefore(end2) && end1.isAfter(start2);
    }

}