package umc.teumteum.server.domain.auth.service;

import umc.teumteum.server.domain.auth.dto.OAuthUserInfo;

public interface OAuthService {
    default OAuthUserInfo getUserInfoWithAccessToken(String accessToken) {
        throw new UnsupportedOperationException("해당 소셜에서는 accessToken 지원하지 않음");
    }

    default OAuthUserInfo getUserInfoWithIdToken(String idToken) {
        throw new UnsupportedOperationException("해당 소셜에서는 idToken 지원하지 않음");
    }
}
