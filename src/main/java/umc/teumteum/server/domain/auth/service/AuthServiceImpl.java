package umc.teumteum.server.domain.auth.service;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.security.SecurityException;
import jakarta.annotation.Resource;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import umc.teumteum.server.domain.auth.converter.AuthConverter;
import umc.teumteum.server.domain.auth.dto.AuthRequestDTO;
import umc.teumteum.server.domain.auth.dto.AuthResponseDTO;
import umc.teumteum.server.domain.auth.dto.OAuthUserInfo;
import umc.teumteum.server.domain.auth.exception.AuthHandler;
import umc.teumteum.server.domain.auth.exception.status.AuthErrorStatus;
import umc.teumteum.server.domain.home.repository.ScheduleRepository;
import umc.teumteum.server.domain.user.entity.User;
import umc.teumteum.server.domain.user.entity.enums.SocialType;
import umc.teumteum.server.domain.user.entity.enums.UserStatus;
import umc.teumteum.server.domain.user.entity.enums.UserStep;
import umc.teumteum.server.domain.user.repository.RoutineRepository;
import umc.teumteum.server.domain.user.service.UserService;
import umc.teumteum.server.global.apiPayload.code.status.ErrorStatus;
import umc.teumteum.server.global.exception.InvalidTokenTypeException;
import umc.teumteum.server.global.jwt.JwtProvider;
import umc.teumteum.server.global.util.S3Util;

import java.time.Duration;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final KakaoOAuthService kakaoOAuthService;
    private final NaverOAuthService naverOAuthService;
    private final UserService userService;

    private final JwtProvider jwtProvider;
    private final S3Util s3Util;

    private final RoutineRepository routineRepository;
    private final ScheduleRepository scheduleRepository;

    @Resource(name = "rtWhitelistRedisTemplate")
    private RedisTemplate<String, String> rtWhitelistRedisTemplate;

    @Resource(name = "atBlacklistRedisTemplate")
    private RedisTemplate<String, String> atBlacklistRedisTemplate;


    @Value("${jwt.access-expiration-ms}")
    private long accessExpirationMs;

    @Value("${jwt.refresh-expiration-ms}")
    private long refreshExpirationMs;


    @Override
    @Transactional
    public AuthResponseDTO.LoginResponse socialLogin(String socialType, AuthRequestDTO.SocialLoginRequest request) {
        // 1. 소셜 로그인 - 사용자 정보 불러오기
        OAuthUserInfo userInfo = getUserInfo(SocialType.valueOf(socialType.toUpperCase()), request.getAccessToken());

        // 2. 사용자 조회 (없으면 생성)
        User user = userService.findOrCreateUser(userInfo);

        // 3. 사용자 status 확인
        if (user.getStatus() == UserStatus.INACTIVE) {
            throw new AuthHandler(ErrorStatus.INACTIVE_USER);
        }

        // 4. 토큰 생성
        String sessionId = UUID.randomUUID().toString();
        String accessToken = jwtProvider.generateAccessToken(user.getId(), sessionId);
        String refreshToken = jwtProvider.generateRefreshToken(user.getId(), sessionId);

        // 5. RT 저장
        String refreshKey = getRefreshKey(user.getId().toString(), sessionId);
        Duration refreshDuration = Duration.ofMillis(refreshExpirationMs);
        rtWhitelistRedisTemplate.opsForValue().set(refreshKey, refreshToken, refreshDuration);

        // 6. 다음 전환할 화면
        UserStep nextStep = user.getStep();
        // 온보딩 중단 예외 고려
        if (nextStep == UserStep.ONBOARDING) {
            // 1) 기본 이미지가 아닌 업로드된 이미지가 있다면 S3에서 삭제 후 초기화
            String currentProfileImage = user.getProfileImageName();
            boolean isCustomImage = !User.DEFAULT_PROFILE_IMAGE.equals(currentProfileImage);

            if (isCustomImage) {
                try {
                    s3Util.deleteObject("profile/" + currentProfileImage);
                } catch (Exception e) {
                    // 삭제 실패 시 로그
                    log.error("S3 프로필 이미지 삭제 실패 - userId : {}, imageName : {}, error : {}",
                            user.getId(), currentProfileImage, e.getMessage());
                }

                // 삭제 성공/실패와 관계없이 DB 프로필 이미지명 초기화
                user.updateProfileImageName(User.DEFAULT_PROFILE_IMAGE);
            }

            // 2) 수면패턴 초기화
            user.clearSleepPattern();

            // 3) 스케줄 초기화 (외래키로 인해 먼저 삭제)
            scheduleRepository.deleteByUser(user);

            // 4) 반복일정 초기화
            routineRepository.deleteByUser(user);
        }

        // 7. converter 작업
        return AuthConverter.toLoginResponse(accessToken, refreshToken, nextStep);
    }


    @Override
    @Transactional
    public AuthResponseDTO.DevTokenResponse generateDevAccessToken() {
        // 1. 더미 사용자 조회 (없으면 생성)
        User masterUser = userService.createDevUser();

        // 2. 토큰 발급
        String sessionId = UUID.randomUUID().toString();
        String accessToken = jwtProvider.generateAccessToken(masterUser.getId(), sessionId);
        String refreshToken = jwtProvider.generateRefreshToken(masterUser.getId(), sessionId);

        // 3. RT 저장
        String refreshKey = getRefreshKey(masterUser.getId().toString(), sessionId);
        Duration refreshDuration = Duration.ofMillis(refreshExpirationMs);
        rtWhitelistRedisTemplate.opsForValue().set(refreshKey, refreshToken, refreshDuration);

        // 4. converter 작업
        return AuthConverter.toDevTokenResponse(accessToken, refreshToken);
    }


    @Override
    public AuthResponseDTO.ReissueResponse reissueToken(AuthRequestDTO.ReissueRequest request) {
        String refreshKey = null;

        try {
            // 1. 전달받은 RT 유효성 검증 (검증 실패 -> 재로그인 필요)
            String refreshToken = request.getRefreshToken();
            validateRefreshTokenForService(refreshToken);

            // 2. 토큰에서 userId와 sessionId 추출
            String userId = jwtProvider.getUserIdFromToken(refreshToken);
            String sessionId = jwtProvider.getSessionIdFromToken(refreshToken);

            // 3. Redis에 저장된 RT 조회 (null -> 재로그인 필요)
            refreshKey = getRefreshKey(userId, sessionId);
            String storedRefreshToken = rtWhitelistRedisTemplate.opsForValue().get(refreshKey);
            if (storedRefreshToken == null) {
                throw new AuthHandler(AuthErrorStatus.REFRESH_TOKEN_NOT_FOUND);
            }

            // 4. Redis에 저장된 RT 유효성 검증 (검증 실패 -> 재로그인 필요)
            validateRefreshTokenForService(storedRefreshToken);

            // 5. 요청받은 RT와 Redis에 저장된 RT 비교 (불일치 -> 재로그인 필요)
            if (!refreshToken.equals(storedRefreshToken)) {
                log.warn("토큰 탈취 의심 - userId: {}, sessionId: {}", userId, sessionId);
                
                // 동일한 sessionID를 갖는 AT 블랙리스트 등록
                String blacklistKey = getBlacklistKey(userId, sessionId);
                Duration blacklistDuration = Duration.ofMillis(accessExpirationMs);
                atBlacklistRedisTemplate.opsForValue().set(blacklistKey, "blacklisted", blacklistDuration);

                throw new AuthHandler(AuthErrorStatus.REFRESH_TOKEN_MISMATCH);
            }

            // 6. 기존 sessionId로 토큰 재발급
            String newAccessToken = jwtProvider.generateAccessToken(Long.valueOf(userId), sessionId);
            String newRefreshToken = jwtProvider.generateRefreshToken(Long.valueOf(userId), sessionId);

            // 7. Redis RT 업데이트
            Duration refreshDuration = Duration.ofMillis(refreshExpirationMs);
            rtWhitelistRedisTemplate.opsForValue().set(refreshKey, newRefreshToken, refreshDuration);

            // 8. converter 작업
            return AuthConverter.toReissueResponse(newAccessToken, newRefreshToken);

        } catch (AuthHandler e) {
            // 예외 발생 시, RT Redis 초기화하여 동일한 sessionID로 토큰 재발급 불가
            if (refreshKey != null) {
                rtWhitelistRedisTemplate.delete(refreshKey);
            }

            throw e;
        }
    }




    // SocialType 따라 로그인 분기 처리
    private OAuthUserInfo getUserInfo(SocialType socialType, String accessToken) {
        switch (socialType) {
            case SocialType.KAKAO:
                return kakaoOAuthService.getUserInfoWithAccessToken(accessToken);
            case SocialType.NAVER:
                return naverOAuthService.getUserInfoWithAccessToken(accessToken);
            default:
                throw new AuthHandler(AuthErrorStatus.INVALID_SOCIAL_TYPE);
        }
    }


    // 서비스용 RT 유효성 검증
    private void validateRefreshTokenForService(String token) {
        try {
            jwtProvider.validateRefreshToken(token);
        } catch (BadCredentialsException e) {
            Throwable cause = e.getCause();

            if (cause instanceof SecurityException) {
                throw new AuthHandler(ErrorStatus.INVALID_JWT_SIGNATURE);
            } else if (cause instanceof MalformedJwtException) {
                throw new AuthHandler(ErrorStatus.MALFORMED_JWT_TOKEN);
            } else if (cause instanceof ExpiredJwtException) {
                throw new AuthHandler(ErrorStatus.EXPIRED_JWT_TOKEN);
            } else if (cause instanceof UnsupportedJwtException) {
                throw new AuthHandler(ErrorStatus.UNSUPPORTED_JWT_TOKEN);
            } else if (cause instanceof IllegalArgumentException) {
                throw new AuthHandler(ErrorStatus.EMPTY_JWT_CLAIMS);
            }
        } catch (InvalidTokenTypeException e) {
            throw new AuthHandler(ErrorStatus.INVALID_TOKEN_TYPE);
        } catch (Exception e) {
            throw new AuthHandler(AuthErrorStatus.INVALID_REFRESH_TOKEN);
        }
    }


    // RT 화이트리스트 키 get
    private String getRefreshKey(String userId, String sessionId) {
        return String.format("RT_WHITELIST:%s:%s", userId, sessionId);
    }


    // AT 블랙리스트 키 get
    private String getBlacklistKey(String userId, String sessionId) {
        return String.format("AT_BLACKLIST:%s:%s", userId, sessionId);
    }
}
