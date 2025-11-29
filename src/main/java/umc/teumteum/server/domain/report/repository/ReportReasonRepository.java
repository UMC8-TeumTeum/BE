package umc.teumteum.server.domain.report.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import umc.teumteum.server.domain.report.entity.ReportReason;

public interface ReportReasonRepository extends JpaRepository<ReportReason, Long> {
}