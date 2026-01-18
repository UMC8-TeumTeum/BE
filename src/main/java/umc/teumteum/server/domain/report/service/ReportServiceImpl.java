package umc.teumteum.server.domain.report.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import umc.teumteum.server.domain.report.converter.ReportConverter;
import umc.teumteum.server.domain.report.dto.ReportRequestDto;
import umc.teumteum.server.domain.report.dto.ReportResponseDto;
import umc.teumteum.server.domain.report.entity.Report;
import umc.teumteum.server.domain.report.entity.ReportReason;
import umc.teumteum.server.domain.report.entity.enums.ReportProcessType;
import umc.teumteum.server.domain.report.entity.enums.ReportStatus;
import umc.teumteum.server.domain.report.entity.enums.TargetType;
import umc.teumteum.server.domain.report.exception.ReportException;
import umc.teumteum.server.domain.report.exception.status.ReportErrorStatus;
import umc.teumteum.server.domain.report.repository.ReportReasonRepository;
import umc.teumteum.server.domain.report.repository.ReportRepository;
import umc.teumteum.server.domain.teum.entity.TeumRequest;
import umc.teumteum.server.domain.teum.entity.TeumResponse;
import umc.teumteum.server.domain.teum.entity.enums.ResponseStatus;
import umc.teumteum.server.domain.teum.repository.TeumRequestRepository;
import umc.teumteum.server.domain.teum.repository.TeumResponseRepository;
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
    private final TeumResponseRepository teumResponseRepository;

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

            if (reporter.getId().equals(targetTeum.getUser().getId())) {
                throw new ReportException(ReportErrorStatus.REPORT_SELF_NOT_ALLOWED);
            }

            // 신고자의 응답 상태를 REPORTED로 변경
            handleTeumRequestReport(reporter, targetTeum);
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
                reason.getTitle()
        );
    }

    @Override
    @Transactional(readOnly = true)
    public ReportResponseDto.ReportDetail getReportDetail(Long reportId) {
        // 신고 내역 조회
        Report report = reportRepository.findById(reportId)
                .orElseThrow(() -> new ReportException(ReportErrorStatus.REPORT_NOT_FOUND));

        // TEUM_REQUEST 타입인데 데이터가 없는 경우를 사전에 차단
        if (report.getTargetType() == TargetType.TEUM_REQUEST && report.getTeumRequest() == null) {
            throw new ReportException(ReportErrorStatus.REPORT_TARGET_NOT_FOUND);
        }

        // 피신고자 특정
        User reportedUser = (report.getTargetType() == TargetType.USER)
                ? report.getTargetUser()
                : report.getTeumRequest().getUser();

        // 누적 신고 횟수 조회
        long totalReportCount = reportRepository.countTotalReportsByUser(reportedUser);

        // DTO 변환 및 반환
        return ReportConverter.toReportDetail(report, totalReportCount);
    }

    @Override
    @Transactional
    public void processReport(Long reportId, ReportRequestDto.ProcessReport request) {
        // 1. 신고 내역 조회
        Report report = reportRepository.findById(reportId)
                .orElseThrow(() -> new ReportException(ReportErrorStatus.REPORT_NOT_FOUND));

        // 2. 이미 완료된 신고인지 확인
        if (report.getStatus() == ReportStatus.RESOLVED) {
            throw new ReportException(ReportErrorStatus.REPORT_ALREADY_RESOLVED);
        }

        // 3. 제재 처리 (SUSPEND일 경우)
        if (request.getProcessType() == ReportProcessType.SUSPEND) {
            // TEUM_REQUEST 타입인데 데이터가 없는 경우
            if (report.getTargetType() == TargetType.TEUM_REQUEST && report.getTeumRequest() == null) {
                throw new ReportException(ReportErrorStatus.REPORT_TARGET_NOT_FOUND);
            }
            // 피신고자 특정
            User reportedUser = (report.getTargetType() == TargetType.USER)
                    ? report.getTargetUser()
                    : report.getTeumRequest().getUser();

            // User 엔티티의 suspend() 호출 (1개월 정지 or 3회 누적 시 영구 탈퇴)
            reportedUser.suspend();
        }

        // 4. 신고 엔티티 상태 업데이트 (RESOLVED)
        report.resolve(request.getProcessType(), request.getAdminMemo());
    }

    /**
     * 틈 요청 신고 시 신고자의 홈에서 숨기기 위한 처리
     */
    private void handleTeumRequestReport(User reporter, TeumRequest targetTeum) {
        TeumResponse response = teumResponseRepository.findRequestAndReceiver(targetTeum.getId(), reporter.getId())
                .orElseThrow(() -> new ReportException(ReportErrorStatus.REPORT_TARGET_NOT_FOUND));

        if (response.getStatus() != ResponseStatus.PENDING) {
            throw new ReportException(ReportErrorStatus.REPORT_INVALID_STATUS); // 혹은 적절한 에러 코드
        }

        // 상태를 REPORTED로 변경
        response.changeStatus(ResponseStatus.REPORTED);
    }
}