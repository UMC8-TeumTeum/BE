package umc.teumteum.server.global.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import umc.teumteum.server.domain.home.entity.Schedule;
import umc.teumteum.server.domain.home.entity.enums.ScheduleType;
import umc.teumteum.server.domain.home.repository.ScheduleRepository;
import umc.teumteum.server.domain.user.entity.Routine;
import umc.teumteum.server.domain.user.entity.User;
import umc.teumteum.server.domain.user.entity.enums.UserStatus;
import umc.teumteum.server.domain.user.entity.enums.Weekday;
import umc.teumteum.server.domain.user.repository.RoutineRepository;
import umc.teumteum.server.domain.user.repository.UserRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class ScheduleGeneratorScheduler {

    private final UserRepository userRepository;
    private final ScheduleRepository scheduleRepository;
    private final RoutineRepository routineRepository;

    @Scheduled(cron = "0 0 0 * * *", zone = "Asia/Seoul")
//    @Scheduled(cron = "0 0/1 * * * *", zone = "Asia/Seoul")
    public void schedule() {
        log.info("[00:00] 반복일정 스케줄 테이블에 등록 시작");

        List<User> users = userRepository.findByStatus(UserStatus.ACTIVE);

        LocalDate today = LocalDate.now();

        for (User user : users) {
            List<Schedule> schedulesToInsert = new ArrayList<>();
            /**
             * routine 테이블의 반복일정 등록
             * 조회방식: 오늘요일 = routine.weekday
             * 이미 스케줄 테이블에 삭제된 루틴으로 저장된 경우 스킵
             * 등록해야할 정보
             * user, routine, title = routine.title, description = routine.description, type=ROUTINE,
             * date(YYYY-MM-DD), startTime(오늘날짜 + routine.startTIme), endTIme(오늘날짜 + routine.endTime)
             */

            Weekday todayWeekday = Weekday.valueOf(today.getDayOfWeek().name());
            List<Routine> routines = routineRepository.findByUserAndWeekday(user, todayWeekday);

            for (Routine routine : routines) {
                // 삭제된 반복일정이라면 스킵
                boolean deletedRoutine = scheduleRepository.existsByUserAndDateAndRoutineAndIsDeletedTrue(user, today, routine);
                if (deletedRoutine) continue;

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
            scheduleRepository.saveAll(schedulesToInsert);
        }
        log.info("[00:00] 총 {}명의 유저에 대해 스케줄 생성 완료", users.size());
    }
}
