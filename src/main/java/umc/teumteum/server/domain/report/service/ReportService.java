package umc.teumteum.server.domain.report.service;

import umc.teumteum.server.domain.report.dto.ReportRequestDto;
import umc.teumteum.server.domain.report.dto.ReportResponseDto;
import umc.teumteum.server.domain.user.entity.User;

public interface ReportService {
    void createReport(User reporter, ReportRequestDto.CreateReport request);
    ReportResponseDto.ReportDetail getReportDetail(Long reportId);
    void processReport(Long reportId, ReportRequestDto.ProcessReport request);
}