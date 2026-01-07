package umc.teumteum.server.domain.auth.service;

import umc.teumteum.server.domain.auth.dto.OAuthUserInfo;

public interface OAuthService {
    default OAuthUserInfo getUserInfoWithAccessToken(String accessToken) {
        throw new UnsupportedOperationException("해당 소셜에서는 AccessToken 방식을 지원하지 않음");
    }

    default OAuthUserInfo getUserInfoWithIdToken(String idToken, String nonce) {
        throw new UnsupportedOperationException("해당 소셜에서는 nonce를 포함한 ID Token 방식을 지원하지 않음");
    }
}