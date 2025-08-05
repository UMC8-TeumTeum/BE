package umc.teumteum.server.domain.teum.dto.shared;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Schema(title = "SharedTeumTimeResponseDto : 함께한 틈 시간 응답 DTO")
public class SharedTeumTimeResponseDto {

    @Schema(description = "함께한 일 수", example = "1")
    private long days;

    @Schema(description = "함께한 시간 수", example = "3")
    private long hours;

    @Schema(description = "함께한 분 수", example = "40")
    private long minutes;

    @Schema(description = "총 시간 (분 단위)", example = "220")
    private long totalMinutes;
}
