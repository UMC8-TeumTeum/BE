package umc.teumteum.server.domain.teum.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import umc.teumteum.server.domain.teum.entity.TeumResponse;
import umc.teumteum.server.domain.teum.entity.enums.RequestStatus;
import umc.teumteum.server.domain.teum.entity.enums.ResponseStatus;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

public interface TeumResponseRepository extends JpaRepository<TeumResponse, Long> {

    // 목록 조회
    @EntityGraph(attributePaths = {
            "teumRequest",
            "teumRequest.user",
            "teumRequest.parentRequest"
    })
    @Query("""
        SELECT r FROM TeumResponse r
        WHERE r.receiverUser.id = :userId
          AND r.status = :respStatus
          AND r.teumRequest.status = :reqStatus
          AND r.teumRequest.user.status = 'ACTIVE'
          AND (r.teumRequest.date > :today OR (r.teumRequest.date = :today AND r.teumRequest.startTime > :now))
          AND NOT EXISTS (
              SELECT 1 FROM Block b 
              WHERE (b.blocker.id = :userId AND b.blocked.id = r.teumRequest.user.id)
                 OR (b.blocker.id = r.teumRequest.user.id AND b.blocked.id = :userId)
          )
    """)
    Page<TeumResponse> findValidPendingResponses(
            @Param("userId") Long userId,
            @Param("today") LocalDate today,
            @Param("now") LocalTime now,
            @Param("respStatus") ResponseStatus respStatus,
            @Param("reqStatus") RequestStatus reqStatus,
            Pageable pageable
    );


    // 달력 날짜 조회 (유령 알림 방지용)
    @Query("""
        SELECT DISTINCT tr.date FROM TeumResponse r
        JOIN r.teumRequest tr
        WHERE r.receiverUser.id = :userId
          AND tr.date BETWEEN :start AND :end
    """)
    List<LocalDate> findReceivedRequestDates(
            @Param("userId") Long userId,
            @Param("start") LocalDate start,
            @Param("end") LocalDate end
    );

    @Query("""
        SELECT r
        FROM TeumResponse r
        JOIN r.teumRequest tr
        WHERE tr.id = :requestId
          AND r.receiverUser.id = :receiverUserId
    """)
    Optional<TeumResponse> findRequestAndReceiver(
            @Param("requestId") Long requestId,
            @Param("receiverUserId") Long receiverUserId
    );
}