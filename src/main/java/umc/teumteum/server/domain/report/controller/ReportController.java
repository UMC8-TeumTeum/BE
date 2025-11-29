package umc.teumteum.server.domain.report.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import umc.teumteum.server.domain.report.dto.ReportRequestDto;
import umc.teumteum.server.domain.report.exception.status.ReportSuccessStatus;
import umc.teumteum.server.domain.report.service.ReportService;
import umc.teumteum.server.domain.user.entity.User;
import umc.teumteum.server.global.annotation.CurrentUser;
import umc.teumteum.server.global.apiPayload.ApiResponse;

@Tag(name = "Report", description = "사용자 신고 관련 API")
@Validated
@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;

    @PostMapping
    @Operation(summary = "신고하기 API", description = "사용자(USER) 또는 틈 요청(TEUM_REQUEST)을 신고합니다. 기타 사유 선택 시 내용을 함께 보내야 합니다.")
    public ApiResponse<Void> createReport(
            @CurrentUser @Parameter(hidden = true) User user,
            @Valid @RequestBody ReportRequestDto.CreateReport request
    ) {
        reportService.createReport(user, request);
        return ApiResponse.of(ReportSuccessStatus.REPORT_CREATED, null);
    }
}