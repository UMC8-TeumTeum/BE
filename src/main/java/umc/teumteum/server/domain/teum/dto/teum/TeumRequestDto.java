package umc.teumteum.server.domain.teum.dto.teum;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.util.List;

@Getter
@NoArgsConstructor
@Schema(title = "TeumRequestDto : 틈 요청 생성 DTO")
public class TeumRequestDto {

    @NotBlank
    @Schema(description = "제목", example = "string")
    private String title;

    @Schema(description = "상세 내용", example = "string")
    private String description;

    @NotBlank
    @Pattern(regexp = "^\\d{4}-\\d{2}-\\d{2}$", message = "날짜 형식은 YYYY-MM-DD이어야 합니다.")
    @Schema(description = "날짜 (YYYY-MM-DD)", example = "YYYY-MM-DD")
    private String date;

    @NotBlank
    @Pattern(regexp = "^\\d{2}:\\d{2}$", message = "시간 형식은 HH:MM이어야 합니다.")
    @Schema(description = "시작 시간 (HH:MM)", example = "00:00")
    private String startTime;

    @NotBlank
    @Pattern(regexp = "^\\d{2}:\\d{2}$", message = "시간 형식은 HH:MM이어야 합니다.")
    @Schema(description = "종료 시간 (HH:MM)", example = "00:00")
    private String endTime;

    @NotNull
    @Schema(description = "그래픽 ID", example = "1")
    private Long graphicId;

    @NotEmpty
    @Schema(description = "수신자 ID 배열", example = "[0, 1]")
    private List<Long> receiverUserIds;

    @Schema(description = "재요청 시 기존 요청 ID (없으면 null)", example = "null")
    private Long parentRequestId;

    @NotNull
    @Schema(description = "(임시) 요청자 ID", example = "1")
    private Long senderUserId;
}
