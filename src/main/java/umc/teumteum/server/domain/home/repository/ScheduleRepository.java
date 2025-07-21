package umc.teumteum.server.domain.home.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import umc.teumteum.server.domain.home.entity.Schedule;
import umc.teumteum.server.domain.home.entity.enums.ScheduleStatus;
import umc.teumteum.server.domain.teum.entity.TeumRequest;
import umc.teumteum.server.domain.user.entity.Routine;
import umc.teumteum.server.domain.user.entity.User;


import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public interface ScheduleRepository extends JpaRepository<Schedule, Long> {
    List<Schedule> findByUserIdAndIncludeTeumIsTrue(Long userId);

    List<Schedule> findByUserIdAndDateAndStatus(Long userId, LocalDate date, ScheduleStatus status);

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

    @Query("""
    SELECT DISTINCT s.date FROM Schedule s
    WHERE s.user.id = :userId
      AND s.type = 'TEUM'
      AND s.status IN (:statuses)
      AND FUNCTION('DATE_FORMAT', s.date, '%Y-%m') = :yearMonth
      AND s.isDeleted = false
""")
    List<LocalDate> findScheduledTeumsByMonth(
            @Param("userId") Long userId,
            @Param("statuses") List<ScheduleStatus> statuses,
            @Param("yearMonth") String yearMonth
    );


    @Query("""
    SELECT s FROM Schedule s
    WHERE s.teumRequest = :teumRequest
      AND s.status IN :statuses
      AND s.type = 'TEUM'
      AND s.isDeleted = false
""")
    List<Schedule> findByTeumRequestAndStatusIn(
            @Param("teumRequest") TeumRequest teumRequest,
            @Param("statuses") List<ScheduleStatus> statuses
    );

    boolean existsByUserAndDateAndRoutineAndIsDeletedTrue(User user, LocalDate today, Routine routine);

    @Query("""
    SELECT s
    FROM Schedule s
    WHERE s.user = :user
      AND s.isDeleted = false
      AND s.startTime < :startOfTomorrow
      AND s.endTime > :startOfToday
    ORDER BY s.startTime ASC
""")
    List<Schedule> findSchedulesOnDate(@Param("user") User user,
                                       @Param("startOfToday") LocalDateTime startOfToday,
                                       @Param("startOfTomorrow") LocalDateTime startOfTomorrow);

    List<Schedule> findByUserIdAndDate(Long id, LocalDate now);

}