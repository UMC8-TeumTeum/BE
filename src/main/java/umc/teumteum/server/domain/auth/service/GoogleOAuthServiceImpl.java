package umc.teumteum.server.domain.auth.service;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken.Payload;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import umc.teumteum.server.domain.auth.converter.AuthConverter;
import umc.teumteum.server.domain.auth.dto.OAuthUserInfo;
import umc.teumteum.server.domain.auth.exception.AuthException;
import umc.teumteum.server.domain.auth.exception.status.AuthErrorStatus;
import umc.teumteum.server.domain.user.entity.enums.SocialType;

@Service
@RequiredArgsConstructor
public class GoogleOAuthServiceImpl implements OAuthService {

    private final GoogleIdTokenVerifier googleIdTokenVerifier;

    // 사용자 정보 조회
    @Override
    public OAuthUserInfo getUserInfoWithIdToken(String idToken) {
        try {
            // 1. ID Token 검증
            GoogleIdToken googleIdToken = googleIdTokenVerifier.verify(idToken);
            if (googleIdToken == null) {
                throw new AuthException(AuthErrorStatus.GOOGLE_USER_INFO_FAILED);
            }

            // 2. 사용자 정보 추출
            Payload payload = googleIdToken.getPayload();
            String socialId = payload.getSubject();
            String email = payload.getEmail();

            // 3. OAuthUserInfo 반환
            return AuthConverter.toOAuthUserInfo(SocialType.GOOGLE, socialId, email);

        } catch (Exception e) {
            throw new AuthException(AuthErrorStatus.GOOGLE_USER_INFO_FAILED);
        }
    }
}
