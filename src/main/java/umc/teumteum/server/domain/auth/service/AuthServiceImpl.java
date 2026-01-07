package umc.teumteum.server.domain.auth.service;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.security.SecurityException;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import java.time.Duration;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import umc.teumteum.server.domain.auth.converter.AuthConverter;
import umc.teumteum.server.domain.auth.dto.*;
import umc.teumteum.server.domain.auth.event.ProfileImageDeleteEvent;
import umc.teumteum.server.domain.auth.exception.AuthException;
import umc.teumteum.server.domain.auth.exception.status.AuthErrorStatus;
import umc.teumteum.server.domain.home.repository.ScheduleRepository;
import umc.teumteum.server.domain.user.entity.User;
import umc.teumteum.server.domain.user.entity.enums.SocialType;
import umc.teumteum.server.domain.user.entity.enums.UserRole;
import umc.teumteum.server.domain.user.entity.enums.UserStep;
import umc.teumteum.server.domain.user.repository.RoutineRepository;
import umc.teumteum.server.domain.user.service.UserService;
import umc.teumteum.server.global.apiPayload.code.status.ErrorStatus;
import umc.teumteum.server.global.exception.InvalidTokenTypeException;
import umc.teumteum.server.global.jwt.JwtProvider;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    @Value("${auth.jwt.access-expiration-ms}")
    private long accessExpirationMs;

    @Value("${auth.jwt.refresh-expiration-ms}")
    private long refreshExpirationMs;

    @Resource(name = "kakaoOAuthServiceImpl") private OAuthService kakaoOAuthService;
    @Resource(name = "naverOAuthServiceImpl") private OAuthService naverOAuthService;
    @Resource(name = "googleOAuthServiceImpl") private OAuthService googleOAuthService;
    private final UserService userService;

    private final JwtProvider jwtProvider;
    private final ApplicationEventPublisher applicationEventPublisher;

    private final RoutineRepository routineRepository;
    private final ScheduleRepository scheduleRepository;

    @Resource(name = "rtWhitelistRedisTemplate") private RedisTemplate<String, String> rtWhitelistRedisTemplate;
    @Resource(name = "atBlacklistRedisTemplate") private RedisTemplate<String, String> atBlacklistRedisTemplate;


    // 인증 - 소셜로그인
    @Override
    @Transactional
    public AuthResponseDto.LoginResponse socialLogin(String socialType, AuthRequestDto.SocialLoginRequest request) {
        // 1. enum 변환
        SocialType type = SocialType.from(socialType);

        // 2. 소셜 로그인 - 사용자 정보 불러오기
        OAuthUserInfo userInfo = getOAuthUserInfo(type, request.getToken(), request.getNonce());

        // 3. 사용자 조회 (없으면 생성)
        User user = userService.findOrCreateUser(userInfo);

        // 4. 온보딩 초기화
        if (user.getStep() == UserStep.ONBOARDING) {
            resetOnboarding(user);
        }

        // 5. 토큰 생성 & RT 저장
        AuthTokens authTokens = issueAndSaveTokens(user.getId(), user.getRole());

        // 6. converter 작업
        return AuthConverter.toLoginResponse(authTokens.getAccessToken(), authTokens.getRefreshToken(), user.getStep());
    }


    // 인증 - 개발용 토큰 발급
    @Override
    @Transactional
    public AuthResponseDto.DevTokenResponse generateDevTokens() {
        // 1. 더미 사용자 조회 (없으면 생성)
        User masterUser = userService.createDevUser();

        // 2. 토큰 생성 & RT 저장
        AuthTokens authTokens = issueAndSaveTokens(masterUser.getId(), masterUser.getRole());

        // 3. converter 작업
        return AuthConverter.toDevTokenResponse(authTokens.getAccessToken(), authTokens.getRefreshToken());
    }


    // 인증 - 토큰 재발급
    @Override
    public AuthResponseDto.ReissueResponse reissueToken(AuthRequestDto.ReissueRequest request) {
        String userId = null;
        String sessionId = null;

        try {
            // 1. 전달받은 RT 유효성 검증
            String refreshToken = request.getRefreshToken();
            validateRefreshTokenOrThrow(refreshToken);

            // 2. 토큰에서 정보 추출
            userId = jwtProvider.getUserIdFromToken(refreshToken);
            sessionId = jwtProvider.getSessionIdFromToken(refreshToken);
            String userRole = jwtProvider.getUserRoleFromToken(refreshToken);

            // 3. Redis RT 확인 및 탈취 감지
            verifyRefreshTokenMatch(userId, sessionId, refreshToken);

            // 4. 기존 sessionId로 토큰 재발급 및 RT 저장
            AuthTokens newTokens = reissueAndSaveTokens(userId, sessionId, userRole);

            // 5. converter 작업
            return AuthConverter.toReissueResponse(newTokens.getAccessToken(), newTokens.getRefreshToken());

        } catch (Exception e) {
            // 예외 발생 시, 동일한 sessionID로 토큰 재발급 불가하도록 Redis RT 삭제
            deleteRefreshTokenWhitelist(userId, sessionId);
            throw e;
        }
    }


    // 인증 - 로그아웃
    @Override
    public void logout(HttpServletRequest httpServletRequest, User user) {
        // 1. Authorization 헤더의 AT로 userId & sessionId 추출
        String userId = user.getId().toString();
        String accessToken = jwtProvider.resolveToken(httpServletRequest);
        String sessionId = jwtProvider.getSessionIdFromToken(accessToken);

        // 2. RT 화이트리스트 삭제
        deleteRefreshTokenWhitelist(userId, sessionId);

        // 3. AT 블랙리스트 저장 (남은 시간만큼)
        long remainingTime = jwtProvider.getRemainingTime(accessToken);
        if (remainingTime > 0) {
            saveAccessTokenBlacklist(userId, sessionId, remainingTime);
        }
    }


    // OAuth 사용자 정보 조회
    private OAuthUserInfo getOAuthUserInfo(SocialType socialType, String token, String nonce) {
        return switch (socialType) {
            case SocialType.KAKAO -> kakaoOAuthService.getUserInfoWithIdToken(token, nonce);
            case SocialType.NAVER -> naverOAuthService.getUserInfoWithAccessToken(token);
            case SocialType.GOOGLE -> googleOAuthService.getUserInfoWithIdToken(token);
        };
    }


    // 토큰 발급&저장
    private AuthTokens issueAndSaveTokens(Long userId, UserRole userRole) {
        // 고유ID & 사용자 ID/ROLE
        String sessionId = UUID.randomUUID().toString();
        String id = userId.toString();
        String role = userRole.toString();

        // 토큰 생성
        AuthTokens authTokens = issueTokens(id, sessionId, role);

        // RT 저장
        saveRefreshTokenWhitelist(id, sessionId, authTokens.getRefreshToken());

        return authTokens;
    }


    // 토큰 발급
    private AuthTokens issueTokens(String userId, String sessionId, String userRole) {
        String accessToken = jwtProvider.generateAccessToken(userId, sessionId, userRole);
        String refreshToken = jwtProvider.generateRefreshToken(userId, sessionId, userRole);
        return AuthConverter.toAuthTokens(accessToken, refreshToken);
    }


    // RT 화이트리스트 저장
    private void saveRefreshTokenWhitelist(String userId, String sessionId, String refreshToken) {
        String refreshKey = getWhitelistKey(userId, sessionId);
        Duration refreshDuration = Duration.ofMillis(refreshExpirationMs);
        rtWhitelistRedisTemplate.opsForValue().set(refreshKey, refreshToken, refreshDuration);
    }


    // 온보딩 내용 초기화
    private void resetOnboarding(User user) {
        // 닉네임 초기화
        user.updateNicknameAndJob(null, null);

        // 이미지 초기화
        String currentProfileImage = user.getProfileImageName();
        if (!User.DEFAULT_PROFILE_IMAGE.equals(currentProfileImage)) {
            // DB 프로필 이미지명 초기화
            user.updateProfileImageName(User.DEFAULT_PROFILE_IMAGE);

            // 이미지 삭제 이벤트 발행
            applicationEventPublisher.publishEvent(
                    ProfileImageDeleteEvent.builder()
                            .userId(user.getId())
                            .imageName(currentProfileImage)
                            .build()
            );
        }

        // 수면패턴 초기화
        user.updateSleepPattern(null, null);

        // 스케줄 초기화 (외래키로 인해 먼저 삭제)
        scheduleRepository.deleteByUser(user);

        // 반복일정 초기화
        routineRepository.deleteByUser(user);
    }


    // RT 유효성 검증
    private void validateRefreshTokenOrThrow(String token) {
        try {
            jwtProvider.validateRefreshToken(token);
        } catch (SecurityException e) {
            throw new AuthException(ErrorStatus.INVALID_JWT_SIGNATURE);
        } catch (MalformedJwtException e) {
            throw new AuthException(ErrorStatus.MALFORMED_JWT_TOKEN);
        } catch (ExpiredJwtException e) {
            throw new AuthException(ErrorStatus.EXPIRED_JWT_TOKEN);
        } catch (UnsupportedJwtException e) {
            throw new AuthException(ErrorStatus.UNSUPPORTED_JWT_TOKEN);
        } catch (IllegalArgumentException e) {
            throw new AuthException(ErrorStatus.EMPTY_JWT_CLAIMS);
        } catch (InvalidTokenTypeException e) {
            throw new AuthException(ErrorStatus.INVALID_TOKEN_TYPE);
        } catch (Exception e) {
            throw new AuthException(AuthErrorStatus.INVALID_REFRESH_TOKEN);
        }
    }


    // RT 화이트리스트 키 get
    private String getWhitelistKey(String userId, String sessionId) {
        return String.format("RT_WHITELIST:%s:%s", userId, sessionId);
    }


    // AT 블랙리스트 키 get
    private String getBlacklistKey(String userId, String sessionId) {
        return String.format("AT_BLACKLIST:%s:%s", userId, sessionId);
    }


    // 토큰 재발급&저장
    private AuthTokens reissueAndSaveTokens(String userId, String sessionId, String userRole) {
        // 토큰 생성
        AuthTokens authTokens = issueTokens(userId, sessionId, userRole);

        // RT 저장
        saveRefreshTokenWhitelist(userId, sessionId, authTokens.getRefreshToken());

        return authTokens;
    }


    // RT 동일한지 확인
    private void verifyRefreshTokenMatch(String userId, String sessionId, String refreshToken) {
        String refreshKey = getWhitelistKey(userId, sessionId);
        String savedRefreshToken = rtWhitelistRedisTemplate.opsForValue().get(refreshKey);

        // Redis에 저장된 RT 없음
        if (savedRefreshToken == null) {
            throw new AuthException(AuthErrorStatus.REFRESH_TOKEN_NOT_FOUND);
        }

        // Redis에 저장된 RT 유효성 검증
        validateRefreshTokenOrThrow(savedRefreshToken);

        // 요청받은 RT와 Redis에 저장된 RT 다름
        if (!refreshToken.equals(savedRefreshToken)) {
            log.warn("[Refresh 토큰 탈취 의심] : 요청 RT != Redis RT - userId: {}, sessionId: {}", userId, sessionId);

            // 동일한 sessionID를 갖는 AT 블랙리스트 저장
            saveAccessTokenBlacklist(userId, sessionId, accessExpirationMs);

            throw new AuthException(AuthErrorStatus.REFRESH_TOKEN_MISMATCH);
        }
    }


    // AT 블랙리스트 저장
    private void saveAccessTokenBlacklist(String userId, String sessionId, long durationMs) {
        String blacklistKey = getBlacklistKey(userId, sessionId);
        Duration blacklistDuration = Duration.ofMillis(durationMs);
        atBlacklistRedisTemplate.opsForValue().set(blacklistKey, "blacklisted", blacklistDuration);
    }


    // RT 화이트리스트 삭제
    private void deleteRefreshTokenWhitelist(String userId, String sessionId) {
        if (userId != null && sessionId != null) {
            String refreshKey = getWhitelistKey(userId, sessionId);
            rtWhitelistRedisTemplate.delete(refreshKey);
        }
    }
}
