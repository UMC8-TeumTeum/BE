package umc.teumteum.server.domain.home.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import umc.teumteum.server.domain.home.entity.Schedule;
import umc.teumteum.server.domain.home.entity.ScheduleReminder;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class ScheduleJdbcRepository {
    private final JdbcTemplate jdbcTemplate;

    @Transactional
    public void saveSchedule(List<Schedule> schedules){
        String sql =  """
        INSERT INTO schedule
          (user_id, routine_id, title, description, 
           date, start_time, end_time, type, status,
           include_teum, is_deleted, is_public,
           created_at, updated_at)
        VALUES
          (?, ?, ?, ?, 
           ?, ?, ?, ?, ?,
           ?, ?, ?,
           NOW(), NOW())
        """;

        jdbcTemplate.batchUpdate(sql,
                new BatchPreparedStatementSetter() {

                    @Override
                    public void setValues(PreparedStatement ps, int i) throws SQLException {
                        Schedule schedule = schedules.get(i);
                        ps.setLong(1,schedule.getUser().getId());
                        ps.setLong(2,schedule.getRoutine().getId());
                        ps.setString(3,schedule.getTitle());
                        ps.setString(4,schedule.getDescription());

                        ps.setObject(5,schedule.getDate());
                        ps.setObject(6,schedule.getStartTime());
                        ps.setObject(7,schedule.getEndTime());
                        ps.setString(8,schedule.getType().toString());
                        ps.setString(9,schedule.getStatus().toString());

                        ps.setBoolean(10,false);
                        ps.setBoolean(11,false);
                        ps.setBoolean(12, false);
                    }

                    @Override
                    public int getBatchSize() {
                        return schedules.size();
                    }
                });
    }

    @Transactional
    public void saveScheduleReminder(List<ScheduleReminder> reminders){
        String sql =  """
        INSERT INTO schedule_reminder
          (schedule_id, reminder_time, alarm_status, 
           created_at, updated_at)
        VALUES
          (?, ?, ?, 
           NOW(), NOW())
        """;

        jdbcTemplate.batchUpdate(sql,
                new BatchPreparedStatementSetter() {

                    @Override
                    public void setValues(PreparedStatement ps, int i) throws SQLException {
                        ScheduleReminder reminder = reminders.get(i);
                        ps.setLong(1, reminder.getSchedule().getId());
                        ps.setInt(2, reminder.getReminderTime());
                        ps.setString(3, reminder.getAlarmStatus().toString());
                    }

                    @Override
                    public int getBatchSize() {
                        return reminders.size();
                    }
                });
    }
}
