package umc.teumteum.server.domain.report.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import umc.teumteum.server.domain.report.entity.Report;

public interface ReportRepository extends JpaRepository<Report, Long> {
}