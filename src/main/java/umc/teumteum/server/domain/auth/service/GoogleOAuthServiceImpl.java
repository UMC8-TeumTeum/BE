package umc.teumteum.server.domain.auth.service;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken.Payload;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import jakarta.annotation.Resource;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import umc.teumteum.server.domain.auth.converter.AuthConverter;
import umc.teumteum.server.domain.auth.dto.OAuthUserInfo;
import umc.teumteum.server.domain.auth.exception.AuthException;
import umc.teumteum.server.domain.auth.exception.status.AuthErrorStatus;
import umc.teumteum.server.domain.user.entity.enums.SocialType;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.time.Duration;

@Slf4j
@Service
@RequiredArgsConstructor
public class GoogleOAuthServiceImpl implements OAuthService {

    private static final long NONCE_TTL_HOURS = 1;

    private final GoogleIdTokenVerifier googleIdTokenVerifier;

    @Resource(name = "nonceRedisTemplate")
    private RedisTemplate<String, String> nonceRedisTemplate;


    @Override
    public OAuthUserInfo getUserInfoWithIdToken(String idToken, String nonce) {
        // 1. Nonce 검증 및 저장
        validateAndSaveNonce(nonce);

        // 2. ID Token 검증
        GoogleIdToken googleIdToken = verifyIdToken(idToken);

        // 3. Nonce 클레임 검증
        verifyNonceClaim(googleIdToken.getPayload(), nonce);

        // 4. 사용자 정보 추출
        Payload payload = googleIdToken.getPayload();
        String socialId = payload.getSubject();
        String email = payload.getEmail();

        // 5. OAuthUserInfo 반환
        return AuthConverter.toOAuthUserInfo(SocialType.GOOGLE, socialId, email);
    }

    private void validateAndSaveNonce(String nonce) {
        // 1. nonce 확인
        if (nonce == null || nonce.isBlank()) {
            throw new AuthException(AuthErrorStatus.NONCE_REQUIRED);
        }

        // 2. Redis 키 생성
        String key = getNonceKey(nonce);

        // 3. Redis에 저장 시도
        Boolean wasUsed = nonceRedisTemplate.opsForValue()
                .setIfAbsent(key, "used", Duration.ofHours(NONCE_TTL_HOURS));

        // 4. 이미 사용된 값이면 wasUsed가 False이기 때문에 예외 처리
        if (Boolean.FALSE.equals(wasUsed)) {
            log.warn("[ID 토큰 탈취 의심] : 이미 사용한 ID Token & nonce로 로그인 시도");
            throw new AuthException(AuthErrorStatus.NONCE_ALREADY_USED);
        }
    }

    private String getNonceKey(String nonce) {
        return String.format("USED_NONCE:%s", nonce);
    }

    private GoogleIdToken verifyIdToken(String idToken){
        try {
            GoogleIdToken googleIdToken = googleIdTokenVerifier.verify(idToken);
            if (googleIdToken == null) {
                throw new AuthException(AuthErrorStatus.GOOGLE_ID_TOKEN_VERIFICATION_FAILED);
            }
            return googleIdToken;
        } catch (GeneralSecurityException | IOException ex) {
            throw new AuthException(AuthErrorStatus.GOOGLE_ID_TOKEN_VERIFICATION_FAILED);
        }
    }

    private void verifyNonceClaim(Payload payload, String nonce) {
        String tokenNonce = (String) payload.get("nonce");
        if (tokenNonce == null || !tokenNonce.equals(nonce)) {
            throw new AuthException(AuthErrorStatus.GOOGLE_ID_TOKEN_VERIFICATION_FAILED);
        }
    }
}
