package umc.teumteum.server.domain.teum.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import umc.teumteum.server.domain.teum.entity.TeumRequest;
import umc.teumteum.server.domain.user.entity.User;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public interface TeumRequestRepository extends JpaRepository<TeumRequest, Long> {
    @Query("SELECT t FROM TeumRequest t " +
            "WHERE t.status = 'ACTIVE' AND " +
            "(t.date < :today OR (t.date = :today AND t.startTime < :nowTime))")
    List<TeumRequest> findAllClosed(@Param("today") java.time.LocalDate today,
                                     @Param("nowTime") java.time.LocalTime nowTime);

    /**
     * 사용자와 관련된 ACTIVE 상태의 TeumRequest 중
     * - 틈 요청에서 파생된 약속된 틈이 없으며
     * - 날짜가 일치하고
     * - 시간대가 겹치는 요청들을 조회
     */
    @Query("""
    SELECT tr FROM TeumRequest tr
    LEFT JOIN tr.teumResponses r
    WHERE tr.status = 'ACTIVE'
      AND (tr.user = :user OR r.receiverUser = :user)
      AND tr.schedules IS EMPTY
      AND tr.date = :date
      AND FUNCTION('TIME', tr.startTime) < :end
      AND FUNCTION('TIME', tr.endTime) > :start
""")
    List<TeumRequest> findConflictingTeumRequest(
            @Param("user") User user,
            @Param("date") LocalDate date,
            @Param("start") LocalTime start,
            @Param("end") LocalTime end
    );

}