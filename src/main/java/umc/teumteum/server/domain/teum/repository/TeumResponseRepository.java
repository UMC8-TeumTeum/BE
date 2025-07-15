package umc.teumteum.server.domain.teum.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import umc.teumteum.server.domain.teum.entity.TeumResponse;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public interface TeumResponseRepository extends JpaRepository<TeumResponse, Long> {

    @Query("""
        SELECT r FROM TeumResponse r
        JOIN FETCH r.teumRequest tr
        JOIN FETCH tr.user sender
        WHERE r.receiverUser.id = :userId
          AND r.status = umc.teumteum.server.domain.teum.entity.enums.ResponseStatus.PENDING
          AND tr.status = umc.teumteum.server.domain.teum.entity.enums.RequestStatus.ACTIVE
          AND (tr.date > :today OR (tr.date = :today AND tr.startTime > :now))
        ORDER BY 
          CASE WHEN r.readAt IS NULL THEN 0 ELSE 1 END ASC,
          r.createdAt DESC
    """)
    List<TeumResponse> findValidPendingResponses(
            @Param("userId") Long userId,
            @Param("today") LocalDate today,
            @Param("now") LocalTime now
    );
}
