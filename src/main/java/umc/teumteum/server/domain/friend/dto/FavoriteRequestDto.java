package umc.teumteum.server.domain.friend.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@Schema(title = "FavoriteRequestDto : 즐겨찾기 설정 요청 DTO")
public class FavoriteRequestDto {

    @Schema(description = "즐겨찾기 설정 여부", example = "true")
    private Boolean isFavorite;
}
