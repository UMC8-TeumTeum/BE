package umc.teumteum.server.domain.home.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
@Schema(title = "TodoIdResponseDTO : 투두 등록 응답 DTO")
public class TodoIdResponseDTO {

    @Schema(description = "todoId" , example = "1")
    private Long todoId;
}
