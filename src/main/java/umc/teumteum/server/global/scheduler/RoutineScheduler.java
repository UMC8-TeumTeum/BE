package umc.teumteum.server.global.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import umc.teumteum.server.domain.home.entity.Schedule;
import umc.teumteum.server.domain.home.entity.ScheduleReminder;
import umc.teumteum.server.domain.home.entity.enums.AlarmStatus;
import umc.teumteum.server.domain.home.entity.enums.RoutineStatus;
import umc.teumteum.server.domain.home.entity.enums.ScheduleType;
import umc.teumteum.server.domain.home.repository.ScheduleJdbcRepository;
import umc.teumteum.server.domain.home.repository.ScheduleRepository;
import umc.teumteum.server.domain.user.entity.RemindAlarm;
import umc.teumteum.server.domain.user.entity.Routine;
import umc.teumteum.server.domain.user.entity.User;
import umc.teumteum.server.domain.user.entity.enums.UserStatus;
import umc.teumteum.server.domain.user.entity.enums.Weekday;
import umc.teumteum.server.domain.user.repository.RemindAlarmRepository;
import umc.teumteum.server.domain.user.repository.RoutineRepository;
import umc.teumteum.server.domain.user.repository.UserRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class RoutineScheduler {

    private final UserRepository userRepository;
    private final ScheduleRepository scheduleRepository;
    private final RoutineRepository routineRepository;
    private final RemindAlarmRepository remindAlarmRepository;
    private final ScheduleJdbcRepository scheduleJdbcRepository;

        @Scheduled(cron = "0 0 0 * * *", zone = "Asia/Seoul")
//    @Scheduled(cron = "0 0/50 * * * *", zone = "Asia/Seoul")
    public void schedule() {
        log.info("[00:00] 반복일정 스케줄 테이블에 등록 시작");

        LocalDate today = LocalDate.now();
        Weekday todayWeekday = Weekday.valueOf(today.getDayOfWeek().name());

        // 1-1. 유저 조회
        List<User> users = userRepository.findByStatus(UserStatus.ACTIVE);

        // 1-2. user : remind alarm 정보 조회
        Map<Long, List<RemindAlarm>> alarmMap = remindAlarmRepository.findAll().stream()
                .collect(Collectors.groupingBy(r -> r.getUser().getId()));

        // 1-3. 오늘 등록할 루틴 조회
        List<Routine> routines = routineRepository.findByWeekday(todayWeekday);

        // 1-4. Schedule 테이블에서 삭제된 루틴 조회
        List<Routine> deletedRoutines = scheduleRepository.findDeletedRoutinesByDate(today);
        Set<Long> deletedRoutineIds = deletedRoutines.stream()
                .map(Routine::getId)
                .collect(Collectors.toSet());

        // 2-1. 생성할 스케줄 모으기
        List<Schedule> schedulesToInsert = new ArrayList<>();

        for (User user : users) {
            // 유저의 루틴 필터링
            List<Routine> userRoutines = routines.stream()
                    .filter(r -> r.getUser().getId().equals(user.getId()))
                    .toList();

            // 스케줄 객체 생성
            for (Routine routine : userRoutines) {
                // 삭제된 반복일정이라면 스킵
                if (deletedRoutineIds.contains(routine.getId())) continue;

                Schedule routineSchedule = Schedule.builder()
                        .user(user)
                        .routine(routine)
                        .title(routine.getTitle())
                        .description(routine.getDescription())
                        .type(ScheduleType.ROUTINE)
                        .date(today)
                        .startTime(LocalDateTime.of(today, routine.getStartTime()))
                        .endTime(LocalDateTime.of(today, routine.getEndTime()))
                        .routineStatus(RoutineStatus.ORIGINAL)
                        .build();
                schedulesToInsert.add(routineSchedule);
            }
        }

        // 2-2. 스케줄 데이터 삽입
        if(!schedulesToInsert.isEmpty()){
            scheduleJdbcRepository.saveSchedule(schedulesToInsert);
        }

        // 3. 생성된 스케줄 데이터 재조회
        Set<Long> userIds = users.stream()
                .map(User::getId)
                .collect(Collectors.toSet());

        List<Schedule> savedRoutines =
                scheduleRepository.findRoutines(today, ScheduleType.ROUTINE, userIds);

        // 4-1. 스케줄 리마인드 데이터 모으기
        List<ScheduleReminder> remindersToInsert = new ArrayList<>();

        for(Schedule routine : savedRoutines){
            List<RemindAlarm> userAlarms = alarmMap.getOrDefault(routine.getUser().getId(), Collections.emptyList());

            for(RemindAlarm alarm : userAlarms){
                remindersToInsert.add(ScheduleReminder.builder()
                        .schedule(routine)
                        .reminderTime(alarm.getMinutesBefore())
                        .alarmStatus(AlarmStatus.INACTIVE)
                        .build());
            }
        }

        // 4-2. 스케줄 리마인드 데이터 삽입
        if(!remindersToInsert.isEmpty()){
            scheduleJdbcRepository.saveScheduleReminder(remindersToInsert);
        }
        log.info("[00:00] 총 {}명의 유저에 대해 스케줄 생성 완료", users.size());
    }
}
