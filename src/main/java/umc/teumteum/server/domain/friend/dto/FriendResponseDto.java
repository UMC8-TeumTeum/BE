package umc.teumteum.server.domain.friend.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

public class FriendResponseDto {

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "맞팔로우 친구 목록 조회 응답")
    public static class MutualFriendDto {

        @Schema(description = "사용자 ID", example = "1")
        private Long userId;

        @Schema(description = "닉네임", example = "틈틈")
        private String nickname;

        @Schema(description = "프로필 이미지 URL", example = "https://example.com/profile.jpg")
        private String profileImageUrl;
    }
}

