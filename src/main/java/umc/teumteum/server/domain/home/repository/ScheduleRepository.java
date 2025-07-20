package umc.teumteum.server.domain.home.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import umc.teumteum.server.domain.home.entity.Schedule;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public interface ScheduleRepository extends JpaRepository<Schedule, Long> {
    List<Schedule> findByUserIdAndIncludeTeumIsTrue(Long userId);

    @Query("""
        SELECT COUNT(s) > 0 FROM Schedule s
        WHERE s.user.id = :userId
          AND s.status = umc.teumteum.server.domain.home.entity.enums.ScheduleStatus.ACTIVE
          AND s.date = :date
          AND s.startTime < :endTime
          AND s.endTime > :startTime
    """)
    boolean existsConflictSchedule(
            @Param("userId") Long userId,
            @Param("date") java.time.LocalDate date,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime
    );

    @Query("""
    SELECT COUNT(s) > 0 FROM Schedule s
    WHERE s.user.id = :userId
      AND s.date = :date
      AND s.startTime = :startTime
      AND s.endTime = :endTime
      AND s.isDeleted = true
""")
    boolean existsDeletedRoutineInstance(
            @Param("userId") Long userId,
            @Param("date") LocalDate date,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime
    );

}