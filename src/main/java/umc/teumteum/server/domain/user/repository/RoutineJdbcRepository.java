package umc.teumteum.server.domain.user.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import umc.teumteum.server.domain.user.entity.Routine;

import java.sql.Time;
import java.sql.Timestamp;
import java.util.List;

@Component
@RequiredArgsConstructor
public class RoutineJdbcRepository {

    private final JdbcTemplate jdbcTemplate;

    public void batchInsertRoutines(List<Routine> routines) {
        Timestamp now = new Timestamp(System.currentTimeMillis());

        String sql = """
            INSERT INTO routine (
                user_id, title, description, weekday, start_time, end_time, 
                created_at, updated_at
            ) VALUES (?, ?, ?, ?, ?, ?, ?, ?)
            """;

        jdbcTemplate.batchUpdate(sql, routines, routines.size(), (ps, routine) -> {
            ps.setLong(1, routine.getUser().getId());
            ps.setString(2, routine.getTitle());
            ps.setString(3, routine.getDescription());
            ps.setString(4, routine.getWeekday().name());
            ps.setTime(5, Time.valueOf(routine.getStartTime()));
            ps.setTime(6, Time.valueOf(routine.getEndTime()));
            ps.setTimestamp(7, now);
            ps.setTimestamp(8, now);
        });
    }
}
