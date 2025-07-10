package umc.teumteum.server.domain.teum.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Schema(title = "TeumResendResponseDto : 재요청(시간 제안) 생성 응답 DTO")
public class TeumResendResponseDto {

    @Schema(description = "생성된 재요청 ID", example = "1")
    private Long id;
}
