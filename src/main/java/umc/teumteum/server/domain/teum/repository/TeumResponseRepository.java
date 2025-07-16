package umc.teumteum.server.domain.teum.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import umc.teumteum.server.domain.teum.entity.TeumResponse;

import java.time.LocalDate;
import java.time.LocalTime;

public interface TeumResponseRepository extends JpaRepository<TeumResponse, Long> {

    @EntityGraph(attributePaths = {"teumRequest", "teumRequest.user"})
    @Query("""
    SELECT r FROM TeumResponse r
    WHERE r.receiverUser.id = :userId
      AND r.status = umc.teumteum.server.domain.teum.entity.enums.ResponseStatus.PENDING
      AND r.teumRequest.status = umc.teumteum.server.domain.teum.entity.enums.RequestStatus.ACTIVE
      AND (r.teumRequest.date > :today OR (r.teumRequest.date = :today AND r.teumRequest.startTime > :now))
""")
    Page<TeumResponse> findValidPendingResponses(
            @Param("userId") Long userId,
            @Param("today") LocalDate today,
            @Param("now") LocalTime now,
            Pageable pageable
    );

}
