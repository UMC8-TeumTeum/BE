package umc.teumteum.server.domain.teum.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Schema(title = "TeumResponseDto : 틈 요청 생성 응답 DTO")
public class TeumResponseDto {

    @Schema(description = "생성된 틈 요청 ID", example = "1")
    private Long id;
}
