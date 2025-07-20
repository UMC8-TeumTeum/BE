package umc.teumteum.server.domain.teum.dto.teum;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import umc.teumteum.server.domain.teum.entity.enums.ResponseStatus;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Schema(title = "TeumStatusResponseDto : 응답 상태 변경 결과 DTO")
public class TeumStatusUpdateResponseDto {

    @Schema(description = "응답 상태", example = "ACCEPTED")
    private ResponseStatus status;

    @Schema(description = "새로운 틈이 생성되었는지 여부 (accepted일 때만 반환)", example = "true")
    private Boolean teumCreated;

    @Schema(description = "생성된 틈 ID (accepted일 때만 반환)", example = "1")
    private Long teumId;
}
