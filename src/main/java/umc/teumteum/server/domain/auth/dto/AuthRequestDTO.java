package umc.teumteum.server.domain.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

public class AuthRequestDTO {

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SocialLoginRequest {

        @NotBlank(message = "소셜 로그인 타입은 필수입니다")
        @Pattern(regexp = "^(kakao|naver)$", message = "지원하지 않는 소셜 로그인 제공자입니다")
        @Schema(description = "소셜 로그인 타입", example = "kakao", allowableValues = {"kakao", "naver"})
        private String socialType;

        @NotBlank(message = "액세스 토큰은 필수입니다")
        @Schema(description = "소셜 로그인 액세스 토큰")
        private String accessToken;
    }
}
