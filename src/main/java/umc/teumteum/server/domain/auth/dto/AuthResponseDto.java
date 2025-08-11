package umc.teumteum.server.domain.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import umc.teumteum.server.domain.user.entity.enums.UserStep;

public class AuthResponseDto {

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "인증 - 소셜 로그인 Response")
    public static class LoginResponse {
        @Schema(description = "틈틈 액세스 토큰", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9")
        private String accessToken;

        @Schema(description = "틈틈 리프레시 토큰", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9")
        private String refreshToken;

        @Schema(description = "다음 전환 화면", example = "ONBOARDING")
        private UserStep nextStep;
    }


    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "인증 - 개발용 액세스 토큰 발급 Response")
    public static class DevTokenResponse {
        @Schema(description = "개발용 액세스 토큰", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9")
        private String accessToken;

        @Schema(description = "개발용 리프레시 토큰", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9")
        private String refreshToken;
    }


    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "인증 - 토큰 재발급 Response")
    public static class ReissueResponse {
        @Schema(description = "새로운 액세스 토큰", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9")
        private String accessToken;

        @Schema(description = "새로운 리프레시 토큰", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9")
        private String refreshToken;
    }
}
