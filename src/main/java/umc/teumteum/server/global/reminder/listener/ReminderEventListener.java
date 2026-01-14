package umc.teumteum.server.global.reminder.listener;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import umc.teumteum.server.domain.home.entity.Schedule;
import umc.teumteum.server.domain.home.entity.ScheduleReminder;
import umc.teumteum.server.domain.home.entity.enums.DispatchStatus;
import umc.teumteum.server.domain.home.repository.ScheduleReminderRepository;
import umc.teumteum.server.domain.notification.entity.enums.NotificationType;
import umc.teumteum.server.domain.notification.service.NotificationOrchestrator;
import umc.teumteum.server.domain.user.entity.User;
import umc.teumteum.server.global.reminder.event.ReminderEvent;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class ReminderEventListener {

    private final ScheduleReminderRepository scheduleReminderRepository;
    private final NotificationOrchestrator orchestrator;

    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleReminderEvent(ReminderEvent event) {

        List<ScheduleReminder> reminders =
                scheduleReminderRepository.findByIdsWithScheduleAndUser(
                        event.reminderIds(),
                        DispatchStatus.PROCESSING
                );

        for (ScheduleReminder r : reminders) {
            try{
                send(r);
                markSent(r.getId());
            } catch (Exception e){
                markFailed(r.getId());
                log.warn("Failed to send reminder [id={}]: {}", r.getId(), e.getMessage(), e);
            }
        }
    }

    // 리마인드 알림 전송
    private void send(ScheduleReminder r){
        // data 구성
        Schedule schedule = r.getSchedule();
        User receiver = schedule.getUser();
        int minutes = r.getReminderTime();

        String content = minutes <= 0 ? "곧 투두가 시작돼요." : minutes + "분 뒤 투두가 시작돼요.";

        Map<String, String> data = new HashMap<>();
        data.put("scheduleId", String.valueOf(schedule.getId()));
        data.put("title", schedule.getTitle());
        data.put("startTime", String.valueOf(schedule.getStartTime()));
        data.put("endTime", String.valueOf(schedule.getEndTime()));
        data.put("reminderMinutes", String.valueOf(r.getReminderTime()));

        // 푸시 알림 전송 로직 위임
        orchestrator.saveAndPush(
                receiver,
                NotificationType.REMIND_ALARM,
                content,
                schedule.getId(),
                data
        );
    }

    // 성공 상태 업데이트
    private void markSent(Long reminderId){
        int updated = scheduleReminderRepository.updateDispatchStatusById(
                reminderId, DispatchStatus.PROCESSING, DispatchStatus.SENT
        );

        if(updated == 0){
            log.warn("Reminder [id={}] already processed.", reminderId);
        }
    }

    // 실패 상태 업데이트
    private void markFailed(Long reminderId){
        int updated = scheduleReminderRepository.updateDispatchStatusById(
                reminderId, DispatchStatus.PROCESSING, DispatchStatus.FAILED
        );

        if(updated == 0){
            log.warn("Reminder [id={}] already processed.", reminderId);
        }
    }
}
