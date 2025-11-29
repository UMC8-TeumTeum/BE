package umc.teumteum.server.domain.report.converter;

import umc.teumteum.server.domain.report.dto.ReportRequestDto;
import umc.teumteum.server.domain.report.entity.Report;
import umc.teumteum.server.domain.report.entity.ReportReason;
import umc.teumteum.server.domain.teum.entity.TeumRequest;
import umc.teumteum.server.domain.user.entity.User;

public class ReportConverter {

    public static Report toReport(User reporter, ReportRequestDto.CreateReport request, ReportReason reason, User targetUser, TeumRequest targetTeum) {

        Report.ReportBuilder reportBuilder = Report.builder()
                .reporter(reporter)
                .reason(reason)
                .targetType(request.getTargetType())
                .targetUser(targetUser)
                .teumRequest(targetTeum);

        // 기타 사유 처리
        if (request.getOtherReason() != null && !request.getOtherReason().isBlank()) {
            reportBuilder.otherReason(request.getOtherReason());
        }

        return reportBuilder.build();
    }
}