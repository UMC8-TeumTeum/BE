package umc.teumteum.server.domain.home.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import umc.teumteum.server.domain.home.entity.ScheduleReminder;

import java.util.List;

public interface ScheduleReminderRepository extends JpaRepository<ScheduleReminder,Long> {
    List<ScheduleReminder> findByScheduleId(Long scheduleId);
    void deleteByScheduleId(Long scheduleId);
    List<ScheduleReminder> findByScheduleIdIn(List<Long> scheduleIds);

    @Modifying(clearAutomatically = true,flushAutomatically = true)
    @Query("DELETE FROM ScheduleReminder sr WHERE sr.schedule.user.id = :userId")
    void deleteAllByUserId(@Param("userId") Long userId);
}
