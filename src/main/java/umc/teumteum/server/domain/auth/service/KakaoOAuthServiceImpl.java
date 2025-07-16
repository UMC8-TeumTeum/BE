package umc.teumteum.server.domain.auth.service;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import umc.teumteum.server.domain.auth.converter.AuthConverter;
import umc.teumteum.server.domain.auth.dto.OAuthUserInfo;
import umc.teumteum.server.domain.user.entity.enums.SocialType;
import umc.teumteum.server.global.apiPayload.code.status.ErrorStatus;
import umc.teumteum.server.global.exception.handler.AuthHandler;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class KakaoOAuthServiceImpl implements KakaoOAuthService {

    private final RestTemplate restTemplate;

    // 사용자 정보 조회
    @Override
    public OAuthUserInfo getUserInfoWithAccessToken(String accessToken) {
        String userInfoUrl = "https://kapi.kakao.com/v2/user/me";

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);

        HttpEntity<Void> request = new HttpEntity<>(headers);

        try {
            ResponseEntity<Map> response = restTemplate.exchange(userInfoUrl, HttpMethod.GET, request, Map.class);
            Map<String, Object> body = response.getBody();

            if (body == null || !body.containsKey("id")) {
                throw new AuthHandler(ErrorStatus.KAKAO_USER_INFO_FAILED);
            }

            String socialId = String.valueOf(body.get("id"));
            Map<String, Object> kakaoAccount = (Map<String, Object>) body.get("kakao_account");
            String email = kakaoAccount != null ? (String) kakaoAccount.get("email") : null;

            return AuthConverter.toOAuthUserInfoDTO(SocialType.KAKAO, socialId, email);

        } catch (Exception e) {
            throw new AuthHandler(ErrorStatus.KAKAO_USER_INFO_FAILED);
        }
    }
}
