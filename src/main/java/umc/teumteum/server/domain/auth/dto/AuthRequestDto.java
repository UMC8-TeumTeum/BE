package umc.teumteum.server.domain.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

public class AuthRequestDto {

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(title = "인증 - 소셜 로그인 Request")
    public static class SocialLoginRequest {

        @NotBlank(message = "소셜 타입별 토큰은 필수입니다.")
        @Schema(description = "소셜 타입별 토큰. 카카오/네이버는 accessToken, 구글은 idToken을 전달", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9")
        private String token;
    }


    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(title = "인증 - 토큰 재발급 Request")
    public static class ReissueRequest {

        @NotBlank(message = "리프레시 토큰은 필수입니다.")
        @Schema(description = "틈틈 리프레시 토큰", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9")
        private String refreshToken;
    }
}
