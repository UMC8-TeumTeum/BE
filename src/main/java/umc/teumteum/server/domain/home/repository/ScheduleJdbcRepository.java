package umc.teumteum.server.domain.home.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import umc.teumteum.server.domain.home.entity.Schedule;

import java.sql.Date;
import java.sql.Timestamp;
import java.util.List;

@Component
@RequiredArgsConstructor
public class ScheduleJdbcRepository {

    private final JdbcTemplate jdbcTemplate;

    public void batchInsertSchedules(List<Schedule> schedules) {
        Timestamp now = new Timestamp(System.currentTimeMillis());

        String sql = """
        INSERT INTO schedule (
            user_id, title, description, type, date, start_time, end_time, 
            is_public, include_teum, status, is_deleted, 
            routine_id, teum_request_id, created_at, updated_at
        ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
        """;

        jdbcTemplate.batchUpdate(sql, schedules, schedules.size(), (ps, schedule) -> {
            ps.setLong(1, schedule.getUser().getId());
            ps.setString(2, schedule.getTitle());
            ps.setString(3, schedule.getDescription());
            ps.setString(4, schedule.getType().name());
            ps.setDate(5, Date.valueOf(schedule.getDate()));
            ps.setTimestamp(6, Timestamp.valueOf(schedule.getStartTime()));
            ps.setTimestamp(7, Timestamp.valueOf(schedule.getEndTime()));
            ps.setBoolean(8, schedule.getIsPublic());
            ps.setBoolean(9, schedule.getIncludeTeum());
            ps.setString(10, schedule.getStatus().name());
            ps.setBoolean(11, schedule.getIsDeleted());
            ps.setObject(12, schedule.getRoutine() != null ? schedule.getRoutine().getId() : null);
            ps.setObject(13, schedule.getTeumRequest() != null ? schedule.getTeumRequest().getId() : null);
            ps.setTimestamp(14, now);
            ps.setTimestamp(15, now);
        });
    }
}
