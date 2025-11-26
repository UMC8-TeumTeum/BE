package umc.teumteum.server.domain.teum.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.time.LocalTime;
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

        @NotNull
        @Schema(description = "수신자 ID (한 명만 가능)", example = "2")
        private Long receiverUserId;

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

        @Schema(description = "응답 상태", example = "ACCEPTED", allowableValues = {"ACCEPTED", "REJECTED"})
        private String status;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @Schema(description = "일정 충돌 확인 요청 DTO")
    public static class ConflictCheckRequest {

        @Schema(description = "확인할 날짜 (YYYY-MM-DD)", example = "2025-05-20", type = "string")
        @NotNull(message = "날짜는 필수입니다.")
        @DateTimeFormat(pattern = "yyyy-MM-dd")
        private LocalDate date;

        @Schema(description = "시작 시간 (HH:mm)", example = "14:00", type = "string")
        @NotNull(message = "시작 시간은 필수입니다.")
        @DateTimeFormat(pattern = "HH:mm")
        private LocalTime startTime;

        @Schema(description = "종료 시간 (HH:mm)", example = "16:00", type = "string")
        @NotNull(message = "종료 시간은 필수입니다.")
        @DateTimeFormat(pattern = "HH:mm")
        private LocalTime endTime;
    }

}
