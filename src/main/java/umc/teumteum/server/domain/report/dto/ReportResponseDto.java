package umc.teumteum.server.domain.report.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import umc.teumteum.server.domain.report.entity.enums.ReportStatus;
import umc.teumteum.server.domain.report.entity.enums.TargetType;

import java.time.LocalDateTime;

public class ReportResponseDto {

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "신고 상세 정보 응답 DTO")
    public static class ReportDetail {

        @Schema(description = "신고 고유 ID", example = "1")
        private Long reportId;

        @Schema(description = "신고 대상 타입 (USER: 사용자, TEUM_REQUEST: 틈 요청)", example = "TEUM_REQUEST")
        private TargetType targetType;

        @Schema(description = "신고 처리 상태 (OPEN: 접수됨, PENDING: 검토중, RESOLVED: 처리완료)", example = "OPEN")
        private ReportStatus status;

        @Schema(description = "선택된 신고 사유 제목", example = "부적절한 언어 사용")
        private String reasonTitle;

        @Schema(description = "직접 입력한 기타 사유 (없을 경우 null)", example = "상대방이 지속적으로 비속어를 사용했습니다.")
        private String otherReason;

        @Schema(description = "신고 일시", example = "2023-12-30T14:30:00")
        private LocalDateTime createdAt;

        @Schema(description = "신고자 정보")
        private UserInfo reporter;

        @Schema(description = "피신고자(대상) 정보 및 누적 통계")
        private TargetUserInfo targetUser;

        @Schema(description = "틈 요청 신고일 경우 포함되는 요청 본문 (USER 신고일 경우 null)")
        private TeumContent teumContent;
    }

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "신고자 기본 정보")
    public static class UserInfo {
        @Schema(description = "사용자 ID", example = "10")
        private Long userId;

        @Schema(description = "사용자 닉네임", example = "나루")
        private String nickname;
    }

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "피신고자 정보 및 신고 통계")
    public static class TargetUserInfo {
        @Schema(description = "피신고자 ID", example = "25")
        private Long userId;

        @Schema(description = "피신고자 닉네임", example = "해빗")
        private String nickname;

        @Schema(description = "해당 유저가 받은 총 신고 누적 횟수", example = "3")
        private long totalReportCount;
    }

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "신고 대상인 틈 요청의 상세 내용")
    public static class TeumContent {
        @Schema(description = "틈 요청 고유 ID", example = "50")
        private Long requestId;

        @Schema(description = "틈 요청 제목", example = "오늘 저녁 같이 먹어요!")
        private String title;

        @Schema(description = "틈 요청 상세 설명", example = "강남역 근처에서 같이 저녁 드실 분 구합니다.")
        private String description;

        @Schema(description = "틈 요청 날짜", example = "2023-12-31")
        private String date;
    }
}