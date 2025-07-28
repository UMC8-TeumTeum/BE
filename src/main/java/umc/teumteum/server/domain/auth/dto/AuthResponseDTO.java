package umc.teumteum.server.domain.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import umc.teumteum.server.domain.user.entity.enums.UserStep;

public class AuthResponseDTO {

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LoginResponse {
        private String accessToken;     // 액세스토큰
        private String refreshToken;    // 리프레시토큰
        private UserStep nextStep;        // 다음 화면 단계
    }

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DevTokenResponse {
        private String accessToken;     // 액세스토큰
    }
}
