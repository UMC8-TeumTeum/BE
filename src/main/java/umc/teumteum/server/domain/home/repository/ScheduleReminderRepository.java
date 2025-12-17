package umc.teumteum.server.domain.home.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import umc.teumteum.server.domain.home.entity.ScheduleReminder;
import umc.teumteum.server.domain.home.entity.enums.AlarmStatus;
import umc.teumteum.server.domain.home.entity.enums.DispatchStatus;

import java.time.LocalDateTime;
import java.util.List;

public interface ScheduleReminderRepository extends JpaRepository<ScheduleReminder,Long> {
    List<ScheduleReminder> findByScheduleId(Long scheduleId);
    void deleteByScheduleId(Long scheduleId);
    List<ScheduleReminder> findByScheduleIdIn(List<Long> scheduleIds);

    // 발송 대상 조회
    @Query("""
        select r from ScheduleReminder r
        join fetch r.schedule s
        join fetch s.user u
        where r.alarmStatus = :alarmStatus
        and r.dispatchStatus = :dispatchStatus
        and r.sendAt <= :now
        order by r.sendAt asc
    """)
    List<ScheduleReminder> findDueWithScheduleAndUser(
            @Param("alarmStatus")AlarmStatus alarmStatus,
            @Param("dispatchStatus") DispatchStatus dispatchStatus,
            @Param("now") LocalDateTime now,
            Pageable pageable
    );

    // 선점 (중복 발송 방지)
    // 선점 성공 1, 실패 0 반환
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
        update ScheduleReminder r
        set r.dispatchStatus = :next
        where r.id = :id
        and r.dispatchStatus = :current
    """)
    int updateDispatchStatus(
            @Param("id") Long id,
            @Param("current") DispatchStatus current,
            @Param("next") DispatchStatus next
    );

    @Modifying
    @Query("""
        update ScheduleReminder r
        set r.dispatchStatus = :next
        where r.id in :ids
        and r.dispatchStatus = :current
    """)
    int updateDispatchStatusByIds(
            @Param("ids") List<Long> ids,
            @Param("current") DispatchStatus current,
            @Param("next") DispatchStatus next
    );


}
