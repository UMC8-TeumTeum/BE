package umc.teumteum.server.domain.auth.service;

import umc.teumteum.server.domain.auth.dto.OAuthUserInfo;

public interface NaverOAuthService {
    OAuthUserInfo getUserInfoWithAccessToken(String accessToken);
}
