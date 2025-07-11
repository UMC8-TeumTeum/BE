package umc.teumteum.server.domain.teum.dto.teum;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@Schema(title = "TeumStatusUpdateRequestDto : 틈 응답 상태 변경 DTO")
public class TeumStatusUpdateRequestDto {

    @Schema(description = "응답 상태", example = "ACCEPTED", allowableValues = {"ACCEPTED", "DECLINED", "SUGGESTED"})
    private TeumResponseStatus status;
}
