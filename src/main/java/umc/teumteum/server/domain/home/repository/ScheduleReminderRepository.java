package umc.teumteum.server.domain.home.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import umc.teumteum.server.domain.home.entity.ScheduleReminder;

import java.util.List;

public interface ScheduleReminderRepository extends JpaRepository<ScheduleReminder,Long> {
    List<ScheduleReminder> findByScheduleId(Long scheduleId);
    void deleteByScheduleId(Long scheduleId);
}
