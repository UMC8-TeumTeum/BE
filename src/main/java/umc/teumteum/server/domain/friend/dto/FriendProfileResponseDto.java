package umc.teumteum.server.domain.friend.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Schema(title = "FriendProfileResponseDto : 친구 프로필 응답 DTO")
public class FriendProfileResponseDto {

    @Schema(description = "사용자 ID", example = "1")
    private Long userId;

    @Schema(description = "이름", example = "string")
    private String name;

    @Schema(description = "프로필 이미지 URL", example = "string")
    private String profileImageUrl;

    @Schema(description = "분야/직종", example = "string")
    private String field;

    @Schema(description = "팔로잉 여부", example = "true")
    private boolean isFollowing;

    @Schema(description = "즐겨찾기 여부", example = "false")
    private boolean isFavorite;
}
