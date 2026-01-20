package umc.teumteum.server.domain.home.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import umc.teumteum.server.domain.home.entity.Schedule;
import umc.teumteum.server.domain.home.entity.enums.RoutineStatus;
import umc.teumteum.server.domain.home.entity.enums.ScheduleStatus;
import umc.teumteum.server.domain.home.entity.enums.ScheduleType;
import umc.teumteum.server.domain.teum.entity.TeumRequest;
import umc.teumteum.server.domain.user.entity.Routine;
import umc.teumteum.server.domain.user.entity.User;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface ScheduleRepository extends JpaRepository<Schedule, Long> {
    List<Schedule> findByUserIdAndIncludeTeumIsTrue(Long userId);

    @Query("""
        SELECT s FROM Schedule s
        WHERE s.user.id = :userId
          AND s.status IN (:statuses)
          AND s.endTime > :checkStart
          AND s.startTime < :checkEnd
    """)
    List<Schedule> findOverlappingSchedules(
            @Param("userId") Long userId,
            @Param("checkStart") LocalDateTime checkStart,
            @Param("checkEnd") LocalDateTime checkEnd,
            @Param("statuses") Collection<ScheduleStatus> statuses
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
      AND s.routineStatus = umc.teumteum.server.domain.home.entity.enums.RoutineStatus.DELETED
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
      AND s.routineStatus <> umc.teumteum.server.domain.home.entity.enums.RoutineStatus.DELETED
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
      AND s.routineStatus <> umc.teumteum.server.domain.home.entity.enums.RoutineStatus.DELETED
""")
    List<Schedule> findByTeumRequestAndStatusIn(
            @Param("teumRequest") TeumRequest teumRequest,
            @Param("statuses") List<ScheduleStatus> statuses
    );

    @Query("""
    SELECT s FROM Schedule s
    WHERE s.user.id = :userId
      AND s.date = :date
      AND s.status IN :statuses
      AND s.routineStatus <> umc.teumteum.server.domain.home.entity.enums.RoutineStatus.DELETED
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
      AND s.routineStatus <> umc.teumteum.server.domain.home.entity.enums.RoutineStatus.DELETED
      AND s.startTime < :startOfTomorrow
      AND s.endTime > :startOfToday
    ORDER BY s.startTime ASC
""")
    List<Schedule> findSchedulesOnDate(@Param("user") User user,
                                       @Param("startOfToday") LocalDateTime startOfToday,
                                       @Param("startOfTomorrow") LocalDateTime startOfTomorrow);


    @Query("""
    SELECT s.routine FROM Schedule s
    WHERE s.date = :date 
    AND s.routineStatus = umc.teumteum.server.domain.home.entity.enums.RoutineStatus.DELETED
    AND s.routine IS NOT NULL""")
    List<Routine> findDeletedRoutinesByDate( @Param("date") LocalDate date);

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
      AND s.routineStatus <> umc.teumteum.server.domain.home.entity.enums.RoutineStatus.DELETED
    ORDER BY s.createdAt DESC
""")
    List<Schedule> findAllPublicByUserId(@Param("userId") Long userId);


    @Query("SELECT s FROM Schedule s WHERE s.user = :user AND s.date BETWEEN :startDate AND :endDate")
    List<Schedule> findByUserAndDateBetween(User user, LocalDate startDate, LocalDate endDate);


    @Query("""
    SELECT DISTINCT s.date FROM Schedule s
    WHERE s.user.id = :userId
      AND s.type IN :types
      AND s.status = 'ACTIVE'
      AND s.isPublic = true
      AND s.routineStatus <> umc.teumteum.server.domain.home.entity.enums.RoutineStatus.DELETED
      AND s.date BETWEEN :start AND :end
""")
    List<LocalDate> findPublicActiveTodos(
            @Param("userId") Long userId,
            @Param("types") Collection<ScheduleType> types,
            @Param("start") LocalDate start,
            @Param("end") LocalDate end
    );

    @Query("""
    SELECT DISTINCT s.date FROM Schedule s
    WHERE s.user.id = :userId
      AND s.type = 'TEUM'
      AND s.status IN :statuses
      AND s.isPublic = true
      AND s.routineStatus <> umc.teumteum.server.domain.home.entity.enums.RoutineStatus.DELETED
      AND s.date BETWEEN :start AND :end
""")
    List<LocalDate> findPublicTeumDates(
            @Param("userId") Long userId,
            @Param("statuses") Collection<ScheduleStatus> statuses,
            @Param("start") LocalDate start,
            @Param("end") LocalDate end
    );


    @Query("SELECT s FROM Schedule s WHERE s.user = :user AND s.date = :date")
    List<Schedule> findByUserAndDate(@Param("user") User user, @Param("date") LocalDate date);


    @Query("""
    SELECT s FROM Schedule s
    WHERE s.user.id = :userId
      AND s.teumRequest.id IN (
          SELECT sr.teumRequest.id FROM Schedule sr
          WHERE sr.user.id = :friendId
            AND sr.type = :type
            AND sr.status = :status
      )
      AND s.type = :type
      AND s.status = :status
""")
    List<Schedule> findMySharedTeumSchedules(
            @Param("userId") Long userId,
            @Param("friendId") Long friendId,
            @Param("type") ScheduleType type,
            @Param("status") ScheduleStatus status
    );

    @Query("""
    SELECT s FROM Schedule s
    WHERE s.user.id = :userId
      AND s.date = :date
      AND s.isPublic = true
      AND s.routineStatus <> umc.teumteum.server.domain.home.entity.enums.RoutineStatus.DELETED
""")
    List<Schedule> findPublicSchedulesByUserAndDate(
            @Param("userId") Long userId,
            @Param("date") LocalDate date
    );



    @Query("""
    SELECT s FROM Schedule s
    WHERE s.user.id = :userId
      AND s.teumRequest.id IN (
          SELECT sr.teumRequest.id FROM Schedule sr
          WHERE sr.user.id = :friendId
            AND sr.type = :type
            AND sr.status = :status
      )
      AND s.type = :type
      AND s.status = :status
""")
    Slice<Schedule> findSharedTeumSchedulesPaged(
            @Param("userId") Long userId,
            @Param("friendId") Long friendId,
            @Param("type") ScheduleType type,
            @Param("status") ScheduleStatus status,
            Pageable pageable
    );

    @Query("""
    SELECT s FROM Schedule s
    WHERE s.type = :type
      AND s.status = :status
      AND s.routineStatus <> umc.teumteum.server.domain.home.entity.enums.RoutineStatus.DELETED
      AND s.startTime <= :now
""")
    List<Schedule> findAllExpiredActiveTeums(
            @Param("now") LocalDateTime now,
            @Param("type") ScheduleType type,
            @Param("status") ScheduleStatus status
    );

    @Query("""
        select s
        from Schedule s
        where s.date = :date
          and s.type = :type
          and s.routine is not null
          and s.user.id in :userIds
          and s.routineStatus <> umc.teumteum.server.domain.home.entity.enums.RoutineStatus.DELETED
    """)
    List<Schedule> findRoutines(
            @Param("date") LocalDate date,
            @Param("type") ScheduleType type,
            @Param("userIds") Collection<Long> userIds
    );

    /**
     * 충돌 스케줄 조회 조건:
     * 1. ACTIVE 상태일 것
     * 2. RoutineStatus가 DELETED가 아닐 것
     * 3. 시간이 겹칠 것 (등호 제외: 접하는 시간 허용)
     */
    @Query("""
    SELECT s FROM Schedule s
    WHERE s.user.id = :userId
      AND s.status = :status
      AND s.routineStatus <> :routineStatus
      AND s.date = :date
      AND s.startTime < :checkEnd
      AND s.endTime > :checkStart
""")
    List<Schedule> findConflictingSchedules(
            @Param("userId") Long userId,
            @Param("date") LocalDate date,
            @Param("checkStart") LocalDateTime checkStart,
            @Param("checkEnd") LocalDateTime checkEnd,
            @Param("status") ScheduleStatus status,        // 추가됨
            @Param("routineStatus") RoutineStatus routineStatus // 추가됨
    );

    /**
     * 오늘 이후의 루틴 검색 (오늘 & 미래)
     */
    @Query("select s from Schedule s where s.routine = :routine and s.date >= :date")
    List<Schedule> findByRoutineAndDateGreaterThanEqual(Routine routine, LocalDate date);

    // 루틴Id로 스케줄 조회
    List<Schedule> findByRoutineId(Long routineId);

    @Modifying(clearAutomatically = true,flushAutomatically = true)
    @Query("DELETE FROM Schedule s WHERE s.user.id = :userId")
    void deleteAllByUserId(@Param("userId") Long userId);


    /**
     *  공개 투두 조회
     *  userId
     */
    @Query("""
    SELECT s FROM Schedule s
    WHERE s.user.id = :userId
      AND s.routineStatus <> umc.teumteum.server.domain.home.entity.enums.RoutineStatus.DELETED
      AND s.date = :date
      AND s.isPublic = true
    """)
    List<Schedule> findByUserAndDateAndIsPublicAndRoutineStatus(
            @Param("userId") Long userId,
            @Param("date") LocalDate date);
}