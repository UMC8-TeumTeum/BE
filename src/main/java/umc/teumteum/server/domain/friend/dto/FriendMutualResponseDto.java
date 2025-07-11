package umc.teumteum.server.domain.friend.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Schema(title = "FriendMutualResponseDto : 맞팔로우 응답 DTO")
public class FriendMutualResponseDto {

    @Schema(description = "유저 ID", example = "0")
    private Long userId;

    @Schema(description = "닉네임", example = "string")
    private String nickname;

    @Schema(description = "프로필 이미지 URL", example = "string")
    private String profileImageUrl;

    @Schema(description = "즐겨찾기 여부", example = "true")
    private Boolean isFavorite;
}

