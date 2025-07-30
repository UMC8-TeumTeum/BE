package umc.teumteum.server.domain.auth.service;

import jakarta.annotation.Resource;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
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
import umc.teumteum.server.global.jwt.JwtProvider;
import umc.teumteum.server.global.util.S3Util;

import java.time.Duration;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthServiceImpl implements AuthService {

    private final KakaoOAuthService kakaoOAuthService;
    private final NaverOAuthService naverOAuthService;
    private final UserService userService;

    private final JwtProvider jwtProvider;
    private final S3Util s3Util;

    private final RoutineRepository routineRepository;
    private final ScheduleRepository scheduleRepository;

    @Resource(name = "rtRedisTemplate")
    private RedisTemplate<String, String> rtRedisTemplate;

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
        String accessToken = jwtProvider.generateAccessToken(user.getId());
        String refreshToken = jwtProvider.generateRefreshToken(user.getId());

        // TODO 기기별 분리 저장 필요
        // 5. 리프레시 토큰 저장
        String key = user.getId().toString();
        Duration refreshDuration = Duration.ofMillis(refreshExpirationMs);
        rtRedisTemplate.opsForValue().set(key, refreshToken, refreshDuration);

        // 6. 다음 전환할 화면
        UserStep nextStep = user.getStep();
        // 온보딩 중단 예외 고려
        if (nextStep == UserStep.ONBOARDING) {
            // 1) 기본 이미지가 아닌 업로드된 이미지가 있다면 S3에서 삭제 후 초기화
            String currentProfileImage = user.getProfileImageName();
            boolean isCustomImage = !User.DEFAULT_PROFILE_IMAGE.equals(currentProfileImage);

            if (isCustomImage) {
                s3Util.deleteObject("profile/" + currentProfileImage);
                user.updateProfileImageName(User.DEFAULT_PROFILE_IMAGE);
            }

            // 2) 수면패턴 초기화
            user.clearSleepPattern();

            // 3) 반복일정 초기화
            routineRepository.deleteByUser(user);

            // 4) 스케줄 초기화
            scheduleRepository.deleteByUser(user);
        }

        // 7. converter 작업
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
                throw new AuthHandler(AuthErrorStatus.INVALID_SOCIAL_TYPE);
        }
    }


    @Override
    @Transactional
    public AuthResponseDTO.DevTokenResponse generateDevAccessToken() {
        // 1. 더미 사용자 조회 (없으면 생성)
        User masterUser = userService.createDevUser();

        // 2. 액세스 토큰 발급
        String accessToken = jwtProvider.generateAccessToken(masterUser.getId());

        // 3. converter 작업
        return AuthConverter.toDevTokenResponse(accessToken);
    }
}
