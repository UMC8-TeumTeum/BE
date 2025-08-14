package umc.teumteum.server.domain.user.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import umc.teumteum.server.domain.user.entity.RemindAlarm;

import java.sql.Timestamp;
import java.util.List;

@Component
@RequiredArgsConstructor
public class RemindAlarmJdbcRepository {

    private final JdbcTemplate jdbcTemplate;

    public void batchInsertRemindAlarms(List<RemindAlarm> remindAlarms) {
        Timestamp now = new Timestamp(System.currentTimeMillis());

        String sql = """
        INSERT INTO remind_alarm (
            user_id, minutes_before, 
            created_at, updated_at
        ) VALUES (?, ?, ?, ?)
        """;

        jdbcTemplate.batchUpdate(sql, remindAlarms, remindAlarms.size(), (ps, remindAlarm) -> {
            ps.setLong(1, remindAlarm.getUser().getId());
            ps.setInt(2, remindAlarm.getMinutesBefore());
            ps.setTimestamp(3, now);
            ps.setTimestamp(4, now);
        });
    }
}
