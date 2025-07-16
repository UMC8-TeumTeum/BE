package umc.teumteum.server.domain.auth.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import umc.teumteum.server.domain.auth.converter.AuthConverter;
import umc.teumteum.server.domain.auth.dto.AuthRequestDTO;
import umc.teumteum.server.domain.auth.dto.AuthResponseDTO;
import umc.teumteum.server.domain.auth.dto.OAuthUserInfo;
import umc.teumteum.server.domain.user.entity.User;
import umc.teumteum.server.domain.user.service.UserService;
import umc.teumteum.server.global.apiPayload.code.status.ErrorStatus;
import umc.teumteum.server.global.exception.handler.AuthHandler;
import umc.teumteum.server.global.util.JwtUtil;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final KakaoOAuthService kakaoOAuthService;
    private final UserService userService;
    private final JwtUtil jwtUtil;

    @Override
    public AuthResponseDTO.LoginResponse socialLogin(AuthRequestDTO.SocialLoginRequest request) {
        // 1. 소셜 로그인 - 사용자 정보 불러오기
        OAuthUserInfo userInfo = getUserInfo(request.getSocialType(), request.getAccessToken());

        // 2. 사용자 조회 (없으면 회원가입)
        User user = userService.findOrCreateUser(userInfo);

        // 3. 토큰 생성
        // 액세스 토큰 생성
        // 리프레시 토큰 생성

        // 4. TODO 리프레시 토큰 저장

        // 5. 다음 단계 결정
        String nextStep = userService.determineUserNextStep(user);

        // 6. converter 작업
        return AuthConverter.toLoginResponse(null, null, nextStep);
    }

    // SocialType 따라 로그인 분기 처리
    private OAuthUserInfo getUserInfo(String socialType, String accessToken) {
        switch (socialType.toUpperCase()) {
            case "KAKAO":
                return kakaoOAuthService.getUserInfoWithAccessToken(accessToken);

//             TODO 네이버 소셜 로그인 구현
//            case "naver":
//                return naverOAuthService.getUserInfoWithAccessToken(accessToken);

            default:
                throw new AuthHandler(ErrorStatus.INVALID_SOCIAL_TYPE);
        }
    }
}
