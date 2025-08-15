package umc.teumteum.server.domain.home.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import umc.teumteum.server.domain.home.entity.ScheduleReminder;

import java.sql.Timestamp;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class ScheduleReminderJdbcRepository {

    private final JdbcTemplate jdbcTemplate;

    public void batchInsertScheduleReminders(List<ScheduleReminder> scheduleReminders) {
        Timestamp now = new Timestamp(System.currentTimeMillis());

        String sql = """
        INSERT INTO schedule_reminder (
            schedule_id, reminder_time, alarm_status, 
            created_at, updated_at
        ) VALUES (?, ?, ?, ?, ?)
        """;

        jdbcTemplate.batchUpdate(sql, scheduleReminders, scheduleReminders.size(), (ps, scheduleReminder) -> {
            ps.setLong(1, scheduleReminder.getSchedule().getId());
            ps.setInt(2, scheduleReminder.getReminderTime());
            ps.setString(3, scheduleReminder.getAlarmStatus().name());
            ps.setTimestamp(4, now);
            ps.setTimestamp(5, now);
        });
    }
}
