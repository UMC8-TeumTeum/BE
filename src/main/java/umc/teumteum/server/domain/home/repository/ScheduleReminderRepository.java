package umc.teumteum.server.domain.home.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import umc.teumteum.server.domain.home.entity.ScheduleReminder;
import umc.teumteum.server.domain.home.entity.enums.AlarmStatus;

import java.util.List;

public interface ScheduleReminderRepository extends JpaRepository<ScheduleReminder,Long> {
    List<ScheduleReminder> findByScheduleIdAndAlarmStatus(Long scheduleId, AlarmStatus alarmStatus);
    void deleteByScheduleId(Long scheduleId);
    List<ScheduleReminder> findByScheduleIdIn(List<Long> scheduleIds);
    List<ScheduleReminder> findByScheduleId(Long scheduleId);
}
