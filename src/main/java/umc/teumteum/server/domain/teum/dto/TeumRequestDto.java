package umc.teumteum.server.domain.teum.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

public class TeumRequestDto {

    @Getter
    @NoArgsConstructor
    @Schema(name = "TeumAvailableTimeRequest", title = "공통 가능 시간 요청")
    public static class TeumAvailableTime {

        @Schema(description = "참여자 ID 목록 (요청자는 자동 포함됨)", example = "[1, 2, 3]")
        private List<Long> userIds;

        @Schema(description = "날짜 (YYYY-MM-DD)", example = "2025-07-30")
        @Pattern(regexp = "^\\d{4}-\\d{2}-\\d{2}$", message = "날짜 형식은 YYYY-MM-DD이어야 합니다.")
        private String date;
    }


    @Getter
    @NoArgsConstructor
    @Schema(title = "TeumRequest : 틈 요청 생성")
    public static class TeumRequest {

        @NotBlank
        @Schema(description = "제목", example = "운동하기")
        private String title;

        @Schema(description = "상세 내용", example = "헬스장 가자")
        private String description;

        @NotBlank
        @Pattern(regexp = "^\\d{4}-\\d{2}-\\d{2}$", message = "날짜 형식은 YYYY-MM-DD이어야 합니다.")
        @Schema(description = "날짜 (YYYY-MM-DD)", example = "2025-08-20")
        private String date;

        @NotBlank
        @Pattern(regexp = "^\\d{2}:\\d{2}$", message = "시간 형식은 HH:MM이어야 합니다.")
        @Schema(description = "시작 시간 (HH:MM)", example = "13:00")
        private String startTime;

        @NotBlank
        @Pattern(regexp = "^\\d{2}:\\d{2}$", message = "시간 형식은 HH:MM이어야 합니다.")
        @Schema(description = "종료 시간 (HH:MM)", example = "14:00")
        private String endTime;

        @NotNull
        @Schema(description = "그래픽 ID", example = "1")
        private Long graphicId;

        @NotEmpty
        @Schema(description = "수신자 ID 배열", example = "[0, 1]")
        private List<Long> receiverUserIds;

    }

    @Getter
    @NoArgsConstructor
    @Schema(title = "TeumResend : 재요청(시간 제안) 생성")
    public static class TeumResend {

        @NotBlank
        @Pattern(regexp = "^\\d{2}:\\d{2}$", message = "시간 형식은 HH:MM이어야 합니다.")
        @Schema(description = "시작 시간 (HH:MM)", example = "13:00")
        private String startTime;

        @NotBlank
        @Pattern(regexp = "^\\d{2}:\\d{2}$", message = "시간 형식은 HH:MM이어야 합니다.")
        @Schema(description = "종료 시간 (HH:MM)", example = "14:00")
        private String endTime;

    }

    @Getter
    @NoArgsConstructor
    @Schema(title = "TeumStatusUpdate : 틈 응답 상태 변경 DTO")
    public static class TeumStatusUpdate {

        @Schema(description = "응답 상태", example = "ACCEPTED", allowableValues = {"ACCEPTED", "DECLINED", "SUGGESTED"})
        private String status;
    }

}
