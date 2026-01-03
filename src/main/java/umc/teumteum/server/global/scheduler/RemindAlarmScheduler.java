package umc.teumteum.server.global.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import umc.teumteum.server.domain.home.entity.ScheduleReminder;
import umc.teumteum.server.domain.home.entity.enums.AlarmStatus;
import umc.teumteum.server.domain.home.entity.enums.DispatchStatus;
import umc.teumteum.server.domain.home.repository.ScheduleReminderRepository;
import umc.teumteum.server.domain.notification.service.NotificationUseCases;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class RemindAlarmScheduler {

    private final ScheduleReminderRepository scheduleReminderRepository;
    private final NotificationUseCases notificationUseCases;

    @Transactional
    @Scheduled(fixedRate = 60000, zone = "Asia/Seoul") // 1분마다 실행
    public void remindAlarm() {
        LocalDateTime now = LocalDateTime.now();

        // 1. 발송 대상 조회 (최대 100개)
        List<ScheduleReminder> targets = scheduleReminderRepository.findDueWithScheduleAndUser(
            AlarmStatus.ACTIVE, DispatchStatus.PENDING, now, PageRequest.of(0, 100)
        );

        if (targets.isEmpty())
            return;

        // 2. 선점하기  (발송 상태 변경하기 PENDING -> PROCESSING)
        List<Long> targetIds = targets.stream().map(ScheduleReminder::getId).toList();

        int lockedCount = scheduleReminderRepository.updateDispatchStatus(targetIds,
            DispatchStatus.PENDING, DispatchStatus.PROCESSING);
        if (lockedCount == 0) return;

        processNotifications(targetIds);

    }
    public void processNotifications(List<Long> targetIds) {

        // 3. 선점 성공한 것들 다시 조회
        List<ScheduleReminder> locked = scheduleReminderRepository.findByIdsWithScheduleAndUser(targetIds,DispatchStatus.PROCESSING);
        if(locked.isEmpty()) return;

        // 4. DB 저장 & 푸시알림 & 상태변경 UseCase에 위임
        try{
            notificationUseCases.notifyReminder(locked);
        } catch(Exception e){
            log.error("리마인드 알림 발송 실패. locked count={}", locked.size(), e);
            // PROCESSING 상태인 리마인더들 -> FAILED로 변경
            List<Long> lockedIds = locked.stream().map(ScheduleReminder::getId).toList();
            scheduleReminderRepository.updateDispatchStatusByIds(
                    lockedIds, DispatchStatus.PROCESSING, DispatchStatus.FAILED
            );
        }
    }
}
