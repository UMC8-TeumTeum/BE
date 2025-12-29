package umc.teumteum.server.domain.report.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import umc.teumteum.server.domain.report.converter.ReportConverter;
import umc.teumteum.server.domain.report.dto.ReportRequestDto;
import umc.teumteum.server.domain.report.entity.Report;
import umc.teumteum.server.domain.report.entity.ReportReason;
import umc.teumteum.server.domain.report.entity.enums.TargetType;
import umc.teumteum.server.domain.report.exception.ReportException;
import umc.teumteum.server.domain.report.exception.status.ReportErrorStatus;
import umc.teumteum.server.domain.report.repository.ReportReasonRepository;
import umc.teumteum.server.domain.report.repository.ReportRepository;
import umc.teumteum.server.domain.teum.entity.TeumRequest;
import umc.teumteum.server.domain.teum.repository.TeumRequestRepository;
import umc.teumteum.server.domain.user.entity.User;
import umc.teumteum.server.domain.user.repository.UserRepository;
import umc.teumteum.server.global.infra.discord.service.DiscordService;

@Service
@RequiredArgsConstructor
@Transactional
public class ReportServiceImpl implements ReportService {

    private final ReportRepository reportRepository;
    private final ReportReasonRepository reportReasonRepository;
    private final UserRepository userRepository;
    private final TeumRequestRepository teumRequestRepository;

    private final DiscordService discordService;

    @Override
    public void createReport(User reporter, ReportRequestDto.CreateReport request) {

        // 신고 사유 조회
        ReportReason reason = reportReasonRepository.findById(request.getReasonId())
                .orElseThrow(() -> new ReportException(ReportErrorStatus.REPORT_REASON_NOT_FOUND));

        // 기타 사유에 내용이 없는 경우
        if (reason.isOther()) {
            if (request.getOtherReason() == null || request.getOtherReason().isBlank()) {
                throw new ReportException(ReportErrorStatus.REPORT_OTHER_REASON_REQUIRED);
            }
        }

        User targetUser = null;
        TeumRequest targetTeum = null;

        // 신고 대상 조회 (분기 처리)
        if (request.getTargetType() == TargetType.USER) {
            // 본인 신고 방지
            if (reporter.getId().equals(request.getTargetId())) {
                throw new ReportException(ReportErrorStatus.REPORT_SELF_NOT_ALLOWED);
            }
            targetUser = userRepository.findById(request.getTargetId())
                    .orElseThrow(() -> new ReportException(ReportErrorStatus.REPORT_TARGET_NOT_FOUND));

        } else if (request.getTargetType() == TargetType.TEUM_REQUEST) {
            targetTeum = teumRequestRepository.findById(request.getTargetId())
                    .orElseThrow(() -> new ReportException(ReportErrorStatus.REPORT_TARGET_NOT_FOUND));
        } else {
            throw new ReportException(ReportErrorStatus.REPORT_INVALID_TARGET_TYPE);
        }

        // Report 생성 및 저장
        Report newReport = ReportConverter.toReport(reporter, request, reason, targetUser, targetTeum);
        Report savedReport = reportRepository.save(newReport);

        // 디스코드 알림 발송
        discordService.sendReportNotification(
                savedReport.getId(),
                savedReport.getTargetType().name(),
                request.getTargetId(),
                reason.getTitle()
        );
    }
}