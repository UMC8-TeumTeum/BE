package umc.teumteum.server.domain.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Schema(title = "PublicTodoResponseDto : 공개 투두 응답 DTO")
public class PublicTodoResponseDto {

    @Schema(description = "투두 제목", example = "string")
    private String title;

    @Schema(description = "시작 시간", example = "00:00")
    private String startTime;

    @Schema(description = "종료 시간", example = "00:00")
    private String endTime;
}
