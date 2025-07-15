package umc.teumteum.server.domain.teum.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import umc.teumteum.server.domain.teum.entity.TeumRequest;

import java.util.List;

public interface TeumRequestRepository extends JpaRepository<TeumRequest, Long> {
    @Query("SELECT t FROM TeumRequest t " +
            "WHERE t.status = 'ACTIVE' AND " +
            "(t.date < :today OR (t.date = :today AND t.startTime < :nowTime))")
    List<TeumRequest> findAllClosed(@Param("today") java.time.LocalDate today,
                                     @Param("nowTime") java.time.LocalTime nowTime);

}