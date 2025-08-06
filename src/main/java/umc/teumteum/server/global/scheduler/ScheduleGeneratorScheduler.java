package umc.teumteum.server.global.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import umc.teumteum.server.domain.home.entity.Schedule;
import umc.teumteum.server.domain.home.entity.ScheduleReminder;
import umc.teumteum.server.domain.home.entity.enums.AlarmStatus;
import umc.teumteum.server.domain.home.entity.enums.ScheduleType;
import umc.teumteum.server.domain.home.repository.ScheduleReminderRepository;
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
public class ScheduleGeneratorScheduler {

    private final UserRepository userRepository;
    private final ScheduleRepository scheduleRepository;
    private final RoutineRepository routineRepository;
    private final RemindAlarmRepository remindAlarmRepository;
    private final ScheduleReminderRepository scheduleReminderRepository;

    @Scheduled(cron = "0 0 0 * * *", zone = "Asia/Seoul")
//    @Scheduled(cron = "0 0/24 * * * *", zone = "Asia/Seoul")
    public void schedule() {
        log.info("[00:00] 반복일정 스케줄 테이블에 등록 시작");

        List<User> users = userRepository.findByStatus(UserStatus.ACTIVE);

        LocalDate today = LocalDate.now();
        Weekday todayWeekday = Weekday.valueOf(today.getDayOfWeek().name());

        // user : remind alarm 정보 미리 조회
        Map<Long, List<RemindAlarm>> alarmMap = remindAlarmRepository.findAll().stream()
                .collect(Collectors.groupingBy(r -> r.getUser().getId()));

        for (User user : users) {
            /**
             * routine 테이블의 반복일정 등록
             * 조회방식: 오늘요일 = routine.weekday
             * 이미 스케줄 테이블에 삭제된 루틴으로 저장된 경우 스킵
             * 등록해야할 정보
             * user, routine, title = routine.title, description = routine.description, type=ROUTINE,
             * date(YYYY-MM-DD), startTime(오늘날짜 + routine.startTIme), endTIme(오늘날짜 + routine.endTime)
             * & schedule reminder 정보
             */
            List<Schedule> schedulesToInsert = new ArrayList<>();
            List<ScheduleReminder> remindersToInsert = new ArrayList<>();

            // 루틴 테이블 조회
            List<Routine> routines = routineRepository.findByUserAndWeekday(user, todayWeekday);

            // Schedule 테이블에서 삭제된 루틴 조회
            List<Routine> deletedRoutines = scheduleRepository.findDeletedRoutinesByUserAndDate(user,today);
            Set<Long> deletedRoutineIds = deletedRoutines.stream()
                    .map(Routine::getId)
                    .collect(Collectors.toSet());

            // 스케줄 저장
            for (Routine routine : routines) {
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
                        .build();
                schedulesToInsert.add(routineSchedule);
            }
            List<Schedule> savedSchedules = scheduleRepository.saveAll(schedulesToInsert);
            List<RemindAlarm> userAlarms = alarmMap.getOrDefault(user.getId(), Collections.emptyList()); // 유저의 리마인드 알림 정보

            // 스케줄 리마인드 저장
            for (Schedule schedule : savedSchedules) {
                for (RemindAlarm alarm : userAlarms) {
                    remindersToInsert.add(ScheduleReminder.builder()
                            .schedule(schedule)
                            .reminderTime(alarm.getMinutesBefore())
                            .alarmStatus(AlarmStatus.INACTIVE)
                            .build());
                }
            }
            scheduleReminderRepository.saveAll(remindersToInsert);
        }
        log.info("[00:00] 총 {}명의 유저에 대해 스케줄 생성 완료", users.size());
    }
}
