package umc.teumteum.server.domain.auth.converter;

import umc.teumteum.server.domain.auth.dto.AuthResponseDTO;
import umc.teumteum.server.domain.auth.dto.OAuthUserInfo;
import umc.teumteum.server.domain.user.entity.enums.SocialType;
import umc.teumteum.server.domain.user.entity.enums.UserStep;

public class AuthConverter {

    public static OAuthUserInfo toOAuthUserInfo (SocialType socialType, String socialId, String email) {
        return OAuthUserInfo.builder()
                .socialType(socialType)
                .socialId(socialId)
                .email(email)
                .build()
                ;
    }

    public static AuthResponseDTO.LoginResponse toLoginResponse(String accessToken, String refreshToken, UserStep nextStep) {
        return AuthResponseDTO.LoginResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .nextStep(nextStep)
                .build()
                ;
    }

    public static AuthResponseDTO.DevTokenResponse toDevTokenResponse(String accessToken) {
        return AuthResponseDTO.DevTokenResponse.builder()
                .accessToken(accessToken)
                .build()
                ;
    }
}
