package umc.teumteum.server.domain.auth.service;

import jakarta.annotation.Resource;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import umc.teumteum.server.domain.auth.converter.AuthConverter;
import umc.teumteum.server.domain.auth.dto.AuthRequestDTO;
import umc.teumteum.server.domain.auth.dto.AuthResponseDTO;
import umc.teumteum.server.domain.auth.dto.OAuthUserInfo;
import umc.teumteum.server.domain.user.entity.User;
import umc.teumteum.server.domain.user.entity.enums.SocialType;
import umc.teumteum.server.domain.user.service.UserService;
import umc.teumteum.server.global.apiPayload.code.status.ErrorStatus;
import umc.teumteum.server.global.exception.handler.AuthHandler;
import umc.teumteum.server.global.util.JwtUtil;

import java.time.Duration;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthServiceImpl implements AuthService {

    private final KakaoOAuthService kakaoOAuthService;
    private final NaverOAuthService naverOAuthService;
    private final UserService userService;
    private final JwtUtil jwtUtil;

    @Resource(name = "rtRedisTemplate")
    private RedisTemplate<String, String> rtRedisTemplate;

    @Value("${jwt.refresh-expiration-ms}")
    private long refreshExpirationMs;

    @Override
    public AuthResponseDTO.LoginResponse socialLogin(String socialType, AuthRequestDTO.SocialLoginRequest request) {
        // 1. 소셜 로그인 - 사용자 정보 불러오기
        OAuthUserInfo userInfo = getUserInfo(SocialType.valueOf(socialType.toUpperCase()), request.getAccessToken());

        // 2. 사용자 조회 (없으면 생성)
        User user = userService.findOrCreateUser(userInfo);

        // 3. 토큰 생성
        String accessToken = jwtUtil.generateAccessToken(user.getId());
        String refreshToken = jwtUtil.generateRefreshToken(user.getId());

        // TODO 기기별 분리 저장 필요
        // 4. 리프레시 토큰 저장
        String key = user.getId().toString();
        Duration refreshDuration = Duration.ofMillis(refreshExpirationMs);
        rtRedisTemplate.opsForValue().set(key, refreshToken, refreshDuration);

        // 5. 다음 단계 결정
        String nextStep = userService.determineUserNextStep(user);

        // 6. converter 작업
        return AuthConverter.toLoginResponse(accessToken, refreshToken, nextStep);
    }

    // SocialType 따라 로그인 분기 처리
    private OAuthUserInfo getUserInfo(SocialType socialType, String accessToken) {
        switch (socialType) {
            case SocialType.KAKAO:
                return kakaoOAuthService.getUserInfoWithAccessToken(accessToken);
            case SocialType.NAVER:
                return naverOAuthService.getUserInfoWithAccessToken(accessToken);
            default:
                throw new AuthHandler(ErrorStatus.INVALID_SOCIAL_TYPE);
        }
    }
}
