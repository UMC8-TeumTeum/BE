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
     * 다음 조건을 모두 만족하는 TeumRequest들을 조회:
     * - 요청자가 해당 사용자(user)인 요청
     * - 상태가 ACTIVE이고 아직 약속된 스케줄이 없음 (schedules.isEmpty)
     * - 요청 날짜(date)가 지정한 날짜와 일치
     * → 시간 겹침 여부는 자바에서 별도 검사
     */
    @Query("""
    SELECT tr FROM TeumRequest tr
    WHERE tr.status = 'ACTIVE'
      AND tr.user = :user
      AND tr.schedules IS EMPTY
      AND tr.date = :date
""")
    List<TeumRequest> findTeumRequestsByUserAndDate(
            @Param("user") User user,
            @Param("date") LocalDate date
    );


}