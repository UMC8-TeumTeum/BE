package umc.teumteum.server.domain.teum.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Schema(title = "SharedTeumResponseDto : 함께한 틈 기록 응답 DTO")
public class SharedTeumResponseDto {

    @Schema(description = "함께 참여한 틈 횟수", example = "1")
    private int sharedTeumCount;

    @Schema(description = "함께한 총 시간 (분 단위)", example = "1")
    private int sharedMinutes;
}
