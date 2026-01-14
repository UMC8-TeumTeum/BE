package umc.teumteum.server.global.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import umc.teumteum.server.domain.home.entity.ScheduleReminder;
import umc.teumteum.server.domain.home.entity.enums.AlarmStatus;
import umc.teumteum.server.domain.home.entity.enums.DispatchStatus;
import umc.teumteum.server.domain.home.repository.ScheduleReminderRepository;
import umc.teumteum.server.global.reminder.event.ReminderEvent;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class RemindAlarmScheduler {

    private final ScheduleReminderRepository scheduleReminderRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    @Scheduled(fixedRate = 60000, zone = "Asia/Seoul")
    public void remindAlarm() {
        LocalDateTime now = LocalDateTime.now();

        // 1. 발송 대상 조회
        List<ScheduleReminder> targets = scheduleReminderRepository.findDueWithScheduleAndUser(
                AlarmStatus.ACTIVE, DispatchStatus.PENDING, now, PageRequest.of(0, 100)
        );

        if(targets.isEmpty()) return;

        // 2. 선점
        List<Long> targetIds = targets.stream().map(ScheduleReminder::getId).toList();

        int locked = scheduleReminderRepository.updateDispatchStatus(
                targetIds,
                DispatchStatus.PENDING,
                DispatchStatus.PROCESSING
        );

        if (locked == 0) return;

        // 3. 선점 성공한 것만 다시 조회 후 이벤트 발생
        List<Long> lockedIds = scheduleReminderRepository.findByIdsWithScheduleAndUser(
                targetIds, DispatchStatus.PROCESSING
        ).stream().map(ScheduleReminder::getId).toList();

        if(!lockedIds.isEmpty()){
            eventPublisher.publishEvent(new ReminderEvent(lockedIds));
        }
    }
}
