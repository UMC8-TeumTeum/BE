package umc.teumteum.server.domain.user.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import umc.teumteum.server.domain.user.entity.RemindAlarm;
import umc.teumteum.server.domain.user.entity.User;

import java.util.List;
import java.util.Optional;

public interface RemindAlarmRepository extends JpaRepository<RemindAlarm, Long> {
    Optional<RemindAlarm> findByUser(User user);
    List<RemindAlarm> findAllByUser(User user);
}