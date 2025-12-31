package umc.teumteum.server.domain.report.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import umc.teumteum.server.domain.report.entity.Report;
import umc.teumteum.server.domain.user.entity.User;

public interface ReportRepository extends JpaRepository<Report, Long> {

    // 유저 직접 신고 + 해당 유저의 틈 요청이 신고된 횟수 합산
    @Query("SELECT COUNT(r) FROM Report r " +
            "LEFT JOIN r.teumRequest tr " +
            "WHERE r.targetUser = :user " +
            "OR tr.user = :user")
    long countTotalReportsByUser(@Param("user") User user);
}