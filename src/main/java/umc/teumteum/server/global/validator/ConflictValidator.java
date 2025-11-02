package umc.teumteum.server.global.validator;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import umc.teumteum.server.domain.home.entity.Schedule;
import umc.teumteum.server.domain.home.entity.enums.RoutineStatus;
import umc.teumteum.server.domain.home.entity.enums.ScheduleStatus;
import umc.teumteum.server.domain.home.entity.enums.ScheduleType;
import umc.teumteum.server.domain.home.repository.ScheduleRepository;
import umc.teumteum.server.domain.teum.entity.TeumRequest;
import umc.teumteum.server.domain.teum.repository.TeumRequestRepository;
import umc.teumteum.server.domain.user.entity.Routine;
import umc.teumteum.server.domain.user.entity.User;
import umc.teumteum.server.domain.user.entity.enums.Weekday;
import umc.teumteum.server.global.exception.GeneralException;
import umc.teumteum.server.global.validator.exception.status.ConflictErrorStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@Component
public class ConflictValidator {

    private final ScheduleRepository scheduleRepository;
    private final TeumRequestRepository teumRequestRepository;

    /**
     * 단일 사용자에 대한 Teum 시간 중복 검증 로직
     * - 사용자가 일정 생성 또는 Teum 응답을 시도할 때 호출
     * - 일정, 반복 일정, 수면 시간과 겹침 여부를 검사
     */
    public void validateTeum(User user, LocalDate date, LocalTime startTime, LocalTime endTime) {
        LocalDateTime startDateTime = LocalDateTime.of(date, startTime);
        LocalDateTime endDateTime = LocalDateTime.of(date, endTime);

        checkWithSchedules(user, startDateTime, endDateTime);
        checkWithRoutines(user, date, startDateTime, endDateTime);
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

    /**
     * TODO 등록 수정시 중복 검증 로직
     * - 1. 수면패턴과 충돌 여부 검사
     * - 2. 미확정된 틈 요청 충돌 여부 검사
     */
    public void validateTodo(User user, LocalDateTime startTime, LocalDateTime endTime) {
        LocalDate date = startTime.toLocalDate();

        checkWithTeumRequests(user,startTime,endTime);
        checkWithSleepPattern(user,date,startTime,endTime);
    }


    // 내부 충돌 검사 메서드들
    private void checkWithSchedules(User user, LocalDateTime requestStart, LocalDateTime requestEnd) {
        List<ScheduleType> types = List.of(ScheduleType.TODO, ScheduleType.WISH, ScheduleType.AI, ScheduleType.TEUM);

        List<Schedule> schedules = scheduleRepository.findSchedulesByUserAndType(user, types);

        for (Schedule schedule : schedules) {

            if(schedule.getType() == ScheduleType.TEUM && schedule.getStatus() == ScheduleStatus.CANCELLED){
                // 취소된 틈 약속의 경우 검사 통과
                continue;
            }
            if (isOverlapping(requestStart, requestEnd, schedule.getStartTime(), schedule.getEndTime())) {
                throw new GeneralException(ConflictErrorStatus.SCHEDULE_CONFLICT);
            }
        }
    }

    private void checkWithTeumRequests(User user, LocalDateTime requestStart, LocalDateTime requestEnd) {
        List<TeumRequest> requests = teumRequestRepository.findTeumRequestsByUserAndDate(user, requestStart.toLocalDate());

        for (TeumRequest request : requests) {
            LocalDateTime teumStart = LocalDateTime.of(request.getDate(), request.getStartTime());
            LocalDateTime teumEnd = LocalDateTime.of(request.getDate(), request.getEndTime());

            // 종료시간이 00:00이면 익일 00:00으로 보정
            if (request.getEndTime().equals(LocalTime.MIDNIGHT)) {
                teumEnd = teumEnd.plusDays(1);
            }

            if (isOverlapping(requestStart, requestEnd, teumStart, teumEnd)) {
                throw new GeneralException(ConflictErrorStatus.TEUM_REQUEST_CONFLICT);
            }
        }
    }

    private void checkWithRoutines(User user, LocalDate date, LocalDateTime requestStart, LocalDateTime requestEnd) {
        Weekday weekday = Weekday.from(date.getDayOfWeek());

        List<Routine> routines = user.getRoutines().stream()
                .filter(r -> r.getWeekday() == weekday)
                .toList();

        for (Routine routine : routines) {
            LocalTime routineStartTime = routine.getStartTime();
            LocalTime routineEndTime = routine.getEndTime();

            // 루틴 시간 → LocalDateTime으로 조합 (자정 넘김 고려)
            LocalDateTime routineStart = LocalDateTime.of(date, routineStartTime);
            LocalDateTime routineEnd = routineStartTime.isBefore(routineEndTime)
                    ? LocalDateTime.of(date, routineEndTime)
                    : LocalDateTime.of(date.plusDays(1), routineEndTime);

            // 시간 겹치는지 판단
            if (isOverlapping(requestStart, requestEnd, routineStart, routineEnd)) {
                // 해당 루틴 기반 스케줄이 있는지 조회
                Optional<Schedule> existing = scheduleRepository.findByUserAndRoutineAndDate(user, routine, date);

                // 반복 일정이 생성 예정 상태 → 충돌
                if (existing.isEmpty()) {
                    throw new GeneralException(ConflictErrorStatus.ROUTINE_CONFLICT);
                }

                // Schedule이 존재하되 isDeleted == false면 → 충돌
                if (!existing.get().getRoutineStatus().equals(RoutineStatus.DELETED)) {
                    throw new GeneralException(ConflictErrorStatus.ROUTINE_CONFLICT);
                }

                // Schedule이 존재하되 isDeleted == true면 -> 충돌 아님
            }
        }
    }


    private void checkWithSleepPattern(User user, LocalDate date, LocalDateTime requestStart, LocalDateTime requestEnd) {
        LocalTime sleepTime = user.getSleepTime();
        LocalTime wakeTime = user.getWakeTime();

        // 수면패턴이 설정되지 않은 경우 -> 충돌 검사 생략
        if (sleepTime == null || wakeTime == null) {
            return;
        }

        // 수면 시간은 이전 날짜 기준으로도 체크 필요
        // 1) 오늘 기준 수면 시간
        LocalDateTime sleepStartToday = LocalDateTime.of(date, sleepTime);
        LocalDateTime sleepEndToday = sleepTime.isBefore(wakeTime)
                ? LocalDateTime.of(date, wakeTime)
                : LocalDateTime.of(date.plusDays(1), wakeTime);

        // 2) 전날 기준 수면 시간 (자정 넘긴 부분 때문에)
        LocalDateTime sleepStartPrev = LocalDateTime.of(date.minusDays(1), sleepTime);
        LocalDateTime sleepEndPrev = sleepTime.isBefore(wakeTime)
                ? LocalDateTime.of(date.minusDays(1), wakeTime)
                : LocalDateTime.of(date, wakeTime);

        if (isOverlapping(requestStart, requestEnd, sleepStartToday, sleepEndToday)
                || isOverlapping(requestStart, requestEnd, sleepStartPrev, sleepEndPrev)) {
            throw new GeneralException(ConflictErrorStatus.SLEEP_PATTERN_CONFLICT);
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