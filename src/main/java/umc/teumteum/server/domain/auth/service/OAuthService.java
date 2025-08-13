package umc.teumteum.server.domain.auth.service;

import umc.teumteum.server.domain.auth.dto.OAuthUserInfo;

public interface OAuthService {
    OAuthUserInfo getUserInfoWithAccessToken(String accessToken);
}
