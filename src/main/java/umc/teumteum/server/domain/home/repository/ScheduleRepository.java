package umc.teumteum.server.domain.home.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import umc.teumteum.server.domain.home.entity.Schedule;
import umc.teumteum.server.domain.home.entity.enums.ScheduleStatus;
import umc.teumteum.server.domain.home.entity.enums.ScheduleType;
import umc.teumteum.server.domain.teum.entity.TeumRequest;
import umc.teumteum.server.domain.user.entity.Routine;
import umc.teumteum.server.domain.user.entity.User;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface ScheduleRepository extends JpaRepository<Schedule, Long> {
    List<Schedule> findByUserIdAndIncludeTeumIsTrue(Long userId);

    @Query("""
    SELECT s FROM Schedule s
    WHERE s.user.id = :userId
      AND s.status = :status
      AND s.endTime >= :startOfDay
      AND s.startTime < :endOfDay
""")
    List<Schedule> findOverlappingSchedules(
            @Param("userId") Long userId,
            @Param("startOfDay") LocalDateTime startOfDay,
            @Param("endOfDay") LocalDateTime endOfDay,
            @Param("status") ScheduleStatus status
    );

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
      AND s.date BETWEEN :start AND :end
      AND s.isDeleted = false
""")
    List<LocalDate> findScheduledTeumsByDates(
            @Param("userId") Long userId,
            @Param("statuses") List<ScheduleStatus> statuses,
            @Param("start") LocalDate start,
            @Param("end") LocalDate end
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

    List<Schedule> findByUserAndDateAndIsDeletedFalseOrderByStartTime(User user, LocalDate date);

    @Query("""
    SELECT s FROM Schedule s
    WHERE s.user.id = :userId
      AND s.date = :date
      AND s.status IN :statuses
      AND s.isDeleted = false
""")
    List<Schedule> findByUserIdAndDateAndStatusIn(
            @Param("userId") Long userId,
            @Param("date") LocalDate date,
            @Param("statuses") List<ScheduleStatus> statuses
    );

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


    @Query("""
    SELECT s.routine FROM Schedule s
    WHERE s.user = :user AND s.date = :date AND s.isDeleted = true AND s.routine IS NOT NULL""")
    List<Routine> findDeletedRoutinesByUserAndDate(@Param("user") User user, @Param("date") LocalDate date);

    /**
     * 사용자 일정 중 다음 조건을 모두 만족하는 일정 조회:
     * - 해당 사용자(user)의 일정이며
     * - 일정 타입이 TODO, WISH, AI 중 하나
     */
    @Query("""
    SELECT s FROM Schedule s
    WHERE s.user = :user
      AND s.type IN :types
""")
    List<Schedule> findSchedulesByUserAndType(
            @Param("user") User user,
            @Param("types") List<ScheduleType> types
    );

    /**
     * 사용자 일정 중 다음 조건을 모두 만족하는 일정 조회:
     * - 해당 사용자(user)의 루틴으로 생성된 일정이며
     * - 주어진 날짜(date)에 생성된 일정
     * - 반복 일정(Routine)의 실제 스케줄 인스턴스 존재 여부 확인에 사용
     */
    @Query("""
    SELECT s FROM Schedule s
    WHERE s.user = :user
      AND s.routine = :routine
      AND s.date = :date
""")
    Optional<Schedule> findByUserAndRoutineAndDate(
            @Param("user") User user,
            @Param("routine") Routine routine,
            @Param("date") LocalDate date
    );

    boolean existsByTeumRequestAndUser(TeumRequest teumRequest, User user);
           
    List<Schedule> findAllByUserAndIncludeTeumTrueAndEndTimeBefore(User user, LocalDateTime now);

    @Query("""
	SELECT s
	FROM Schedule s
	WHERE s.user.id = :userId
	  AND s.includeTeum = true
	  AND s.endTime < :now
	  AND s.type IN :types
""")
    List<Schedule> findSchedulesForTeumTime(
            @Param("userId") Long userId,
            @Param("now") LocalDateTime now,
            @Param("types") List<ScheduleType> types
    );

    @Query("SELECT s FROM Schedule s JOIN FETCH s.user WHERE s.date = :date")
    List<Schedule> findAllByDateWithUser(@Param("date") LocalDate today);

    List<Schedule> findByUser(User user);


    @Modifying
    @Query("delete from Schedule s where s.user = :user")
    void deleteByUser(@Param("user") User user);

    @Query("""
    SELECT s FROM Schedule s
    WHERE s.user.id = :userId
      AND s.isPublic = true
      AND s.isDeleted = false
    ORDER BY s.createdAt DESC
""")
    List<Schedule> findAllPublicByUserId(@Param("userId") Long userId);



    @Query("SELECT s FROM Schedule s WHERE s.user = :user AND s.date BETWEEN :startDate AND :endDate")
    List<Schedule> findByUserAndDateBetween(User user, LocalDate startDate, LocalDate endDate);
}