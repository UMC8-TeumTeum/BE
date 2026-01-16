package umc.teumteum.server.domain.report.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import umc.teumteum.server.domain.report.entity.enums.ReportProcessType;
import umc.teumteum.server.domain.report.entity.enums.TargetType;

public class ReportRequestDto {

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(title = "신고하기: 사용자 혹은 틈 요청을 신고하는 DTO")
    public static class CreateReport {

        @NotNull(message = "신고 대상 타입은 필수입니다. (USER 또는 TEUM_REQUEST)")
        @Schema(description = "신고 대상의 타입입니다. (사용자 신고: USER, 틈 요청 신고: TEUM_REQUEST)", example = "USER")
        private TargetType targetType;

        @NotNull(message = "신고 대상의 ID는 필수입니다.")
        @Schema(description = "신고할 대상의 Primary Key(ID)입니다. targetType이 USER면 user_id, TEUM_REQUEST면 teum_request_id를 넣어주세요.", example = "1")
        private Long targetId;

        @NotNull(message = "신고 사유 ID는 필수입니다.")
        @Schema(description = "신고 사유의 ID입니다. (1: 혐오발언, 2: 성희롱, ... 7: 기타)", example = "7")
        private Long reasonId;

        @Schema(description = "기타 사유(reasonId가 7일 때)를 선택했을 경우 구체적인 내용을 입력합니다. 그 외에는 null을 보내주세요.", example = "상대방이 지속적으로 비속어를 사용합니다.")
        private String otherReason;
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(title = "신고 처리: 관리자가 신고를 처리하는 DTO")
    public static class ProcessReport {

        @NotNull(message = "처리 방식은 필수입니다.")
        @Schema(description = "처리 방식 (SUSPEND: 정지, DISMISS: 반려)", example = "SUSPEND")
        private ReportProcessType processType;

        @Schema(description = "관리자 처리 사유 및 메모", example = "부적절한 언어 사용 반복 확인으로 1개월 정지 처리함")
        private String adminMemo;
    }
}