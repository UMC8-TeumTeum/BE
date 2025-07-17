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

        @NotBlank(message = "액세스 토큰은 필수입니다")
        @Schema(description = "소셜 로그인 액세스 토큰")
        private String accessToken;
    }
}
