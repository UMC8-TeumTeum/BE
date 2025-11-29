package umc.teumteum.server.domain.report.service;

import umc.teumteum.server.domain.report.dto.ReportRequestDto;
import umc.teumteum.server.domain.user.entity.User;

public interface ReportService {
    void createReport(User reporter, ReportRequestDto.CreateReport request);
}