package umc.teumteum.server.domain.friend.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Schema(title = "FavoriteResponseDto : 즐겨찾기 상태 응답 DTO")
public class FavoriteResponseDto {

    @Schema(description = "유저 ID", example = "0")
    private Long userId;

    @Schema(description = "현재 즐겨찾기 상태", example = "true")
    private Boolean isFavorite;
}
