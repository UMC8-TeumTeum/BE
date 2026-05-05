package umc.teumteum.server.domain.auth.service;

import com.auth0.jwk.Jwk;
import com.auth0.jwk.JwkException;
import com.auth0.jwk.JwkProvider;
import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import jakarta.annotation.Resource;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import umc.teumteum.server.domain.auth.converter.AuthConverter;
import umc.teumteum.server.domain.auth.dto.OAuthUserInfo;
import umc.teumteum.server.domain.auth.exception.AuthException;
import umc.teumteum.server.domain.auth.exception.status.AuthErrorStatus;
import umc.teumteum.server.domain.user.entity.enums.SocialType;
import umc.teumteum.server.global.util.LogHashUtil;

import java.security.interfaces.RSAPublicKey;
import java.time.Duration;

@Slf4j
@Service
@RequiredArgsConstructor
public class KakaoOAuthServiceImpl implements OAuthService {

    private static final String KAKAO_ISSUER = "https://kauth.kakao.com";
    private static final long NONCE_TTL_HOURS = 12;

    @Value("${auth.kakao.platform-key}")
    private String platformKey;

    private final JwkProvider kakaoJwkProvider;
    private final LogHashUtil logHashUtil;

    @Resource(name = "nonceRedisTemplate")
    private RedisTemplate<String, String> nonceRedisTemplate;


    @Override
    public OAuthUserInfo getUserInfoWithIdToken(String idToken, String nonce) {
        // 1. nonce 필수값 검증
        validateNonceNotBlank(nonce);

        // 2. ID Token 검증
        DecodedJWT verifiedJwt = verifyIdToken(idToken, nonce);

        // 3. 사용자 정보 추출
        String socialId = verifiedJwt.getSubject();
        String email = verifiedJwt.getClaim("email").asString();

        // 4. nonce 재사용 검증 및 저장
        validateAndSaveNonce(nonce, socialId);

        // 5. OAuthUserInfo 반환
        return AuthConverter.toOAuthUserInfo(SocialType.KAKAO, socialId, email);
    }

    private void validateNonceNotBlank(String nonce) {
        if (nonce == null || nonce.isBlank()) {
            throw new AuthException(AuthErrorStatus.NONCE_REQUIRED);
        }
    }

    private void validateAndSaveNonce(String nonce, String socialId) {
        // 1. Redis 키 생성
        String key = getNonceKey(nonce);

        // 2. Redis에 저장 시도
        Boolean wasUsed = nonceRedisTemplate.opsForValue()
                .setIfAbsent(key, "used", Duration.ofHours(NONCE_TTL_HOURS));

        // 3. 이미 사용된 값이면 wasUsed가 False이기 때문에 예외 처리
        if (Boolean.FALSE.equals(wasUsed)) {
            log.warn("[ID Token Replay Suspected] provider=KAKAO, id={}",
                    logHashUtil.hashIdentifier(socialId));
            throw new AuthException(AuthErrorStatus.NONCE_ALREADY_USED);
        }
    }

    private String getNonceKey(String nonce) {
        return String.format("USED_NONCE:%s", nonce);
    }

    private DecodedJWT verifyIdToken(String idToken, String nonce) {
        try {
            // 1. 파싱해서 kid 가져오기
            DecodedJWT jwt = JWT.decode(idToken);
            String kid = jwt.getKeyId();

            // 2. 공개키로 RSA256 알고리즘 생성
            Algorithm algorithm = getAlgorithm(kid);

            // 3. ID Token 검증 및 반환
            return JWT.require(algorithm)
                    .withIssuer(KAKAO_ISSUER)
                    .withAudience(platformKey)
                    .withClaim("nonce", nonce)
                    .acceptLeeway(10)
                    .build()
                    .verify(idToken);
        } catch (JwkException | JWTVerificationException e) {
            log.error("카카오 ID Token 검증 실패 - {}", e.getMessage());
            throw new AuthException(AuthErrorStatus.KAKAO_ID_TOKEN_VERIFICATION_FAILED);
        }
    }

    private Algorithm getAlgorithm(String kid) throws JwkException {
        // 1. kid로 공개키 찾기
        Jwk jwk = kakaoJwkProvider.get(kid);
        RSAPublicKey publicKey = (RSAPublicKey) jwk.getPublicKey();

        // 2. 알고리즘 생성
        return Algorithm.RSA256(publicKey, null);
    }
}
