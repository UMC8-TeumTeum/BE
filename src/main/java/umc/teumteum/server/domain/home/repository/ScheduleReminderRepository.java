package umc.teumteum.server.domain.home.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import umc.teumteum.server.domain.home.entity.ScheduleReminder;

public interface ScheduleReminderRepository extends JpaRepository<ScheduleReminder,Long> {
}
