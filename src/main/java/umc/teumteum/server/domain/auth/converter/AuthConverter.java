package umc.teumteum.server.domain.auth.converter;

import umc.teumteum.server.domain.auth.dto.AuthResponseDto;
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

    public static AuthResponseDto.LoginResponse toLoginResponse(String accessToken, String refreshToken, UserStep nextStep) {
        return AuthResponseDto.LoginResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .nextStep(nextStep)
                .build()
                ;
    }

    public static AuthResponseDto.DevTokenResponse toDevTokenResponse(String accessToken, String refreshToken) {
        return AuthResponseDto.DevTokenResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build()
                ;
    }

    public static AuthResponseDto.ReissueResponse toReissueResponse(String newAccessToken, String newRefreshToken) {
        return AuthResponseDto.ReissueResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(newRefreshToken)
                .build()
                ;
    }
}
