package umc.teumteum.server.domain.friend.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

public class FriendRequestDto {

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "팔로잉 즐겨찾기 설정/해제 요청")
    public static class FriendFavorite {

        @NotNull(message = "즐겨찾기 여부는 필수 입력입니다.")
        @Schema(description = "즐겨찾기 여부 (true=설정, false=해제)", example = "true")
        private Boolean isFavorite;
    }
}

