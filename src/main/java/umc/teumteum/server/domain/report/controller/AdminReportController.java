package umc.teumteum.server.domain.report.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import umc.teumteum.server.domain.report.dto.ReportRequestDto;
import umc.teumteum.server.domain.report.dto.ReportResponseDto;
import umc.teumteum.server.domain.report.exception.status.ReportSuccessStatus;
import umc.teumteum.server.domain.report.service.ReportService;
import umc.teumteum.server.global.apiPayload.ApiResponse;

@Tag(name = "Admin Report", description = "관리자용 신고 관리 API")
@RestController
@RequestMapping("/api/v1/admin/reports")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminReportController {

    private final ReportService reportService;

    @Operation(summary = "신고 상세 조회", description = "신고의 상세 내용과 피신고자의 누적 신고 횟수를 조회합니다.")
    @GetMapping("/{reportId}")
    public ApiResponse<ReportResponseDto.ReportDetail> getReportDetail(
            @PathVariable Long reportId
    ) {
        ReportResponseDto.ReportDetail response
                = reportService.getReportDetail(reportId);

        return ApiResponse.of(ReportSuccessStatus.REPORT_DETAIL_FETCHED, response);
    }

    @Operation(summary = "신고 처리 (피드백)", description = "관리자가 신고에 대해 정지 또는 반려 처리를 진행합니다.")
    @PatchMapping("/{reportId}/process")
    public ApiResponse<Void> processReport(
            @PathVariable Long reportId,
            @RequestBody ReportRequestDto.ProcessReport request
    ) {
        reportService.processReport(reportId, request);
        return ApiResponse.of(ReportSuccessStatus.REPORT_PROCESSED, null);
    }
}