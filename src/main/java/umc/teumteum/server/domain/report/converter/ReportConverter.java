package umc.teumteum.server.domain.report.converter;

import umc.teumteum.server.domain.report.dto.ReportRequestDto;
import umc.teumteum.server.domain.report.dto.ReportResponseDto;
import umc.teumteum.server.domain.report.entity.Report;
import umc.teumteum.server.domain.report.entity.ReportReason;
import umc.teumteum.server.domain.report.entity.enums.TargetType;
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
        if (reason.isOther() && request.getOtherReason() != null) {
            // trim으로 앞뒤 공백 제거
            reportBuilder.otherReason(request.getOtherReason().trim());
        }

        return reportBuilder.build();
    }

    public static ReportResponseDto.ReportDetail toReportDetail(Report report, long totalReportCount) {
        // 피신고자(대상 유저) 특정
        User reportedUser = (report.getTargetType() == TargetType.USER)
                ? report.getTargetUser()
                : report.getTeumRequest().getUser();

        ReportResponseDto.ReportDetail.ReportDetailBuilder builder = ReportResponseDto.ReportDetail.builder()
                .reportId(report.getId())
                .targetType(report.getTargetType())
                .status(report.getStatus())
                .reasonTitle(report.getReason().getTitle())
                .otherReason(report.getOtherReason())
                .createdAt(report.getCreatedAt())
                .reporter(ReportResponseDto.UserInfo.builder()
                        .userId(report.getReporter().getId())
                        .nickname(report.getReporter().getNickname())
                        .build())
                .targetUser(ReportResponseDto.TargetUserInfo.builder()
                        .userId(reportedUser.getId())
                        .nickname(reportedUser.getNickname())
                        .totalReportCount(totalReportCount)
                        .build());

        // 틈 요청 신고인 경우 날짜까지만 포함
        if (report.getTargetType() == TargetType.TEUM_REQUEST && report.getTeumRequest() != null) {
            var teum = report.getTeumRequest();
            builder.teumContent(ReportResponseDto.TeumContent.builder()
                    .requestId(teum.getId())
                    .title(teum.getTitle())
                    .description(teum.getDescription())
                    .date(teum.getDate().toString())
                    .build());
        }

        return builder.build();
    }
}