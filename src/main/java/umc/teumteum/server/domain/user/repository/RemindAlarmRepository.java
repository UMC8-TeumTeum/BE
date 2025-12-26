package umc.teumteum.server.domain.user.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import umc.teumteum.server.domain.user.entity.RemindAlarm;
import umc.teumteum.server.domain.user.entity.User;

import java.util.Collection;
import java.util.List;

public interface RemindAlarmRepository extends JpaRepository<RemindAlarm, Long> {
    List<RemindAlarm> findAllByUser(User user);
    boolean existsByUser(User user);
    void deleteByUserAndMinutesBeforeIn(User user, Collection<Integer> minutesBeforeList);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("DELETE FROM RemindAlarm ra WHERE ra.user.id = :userId")
    void deleteAllByUserId(@Param("userId") Long userId);
}