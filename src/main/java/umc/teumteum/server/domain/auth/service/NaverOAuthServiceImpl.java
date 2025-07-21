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
import umc.teumteum.server.domain.auth.exception.status.AuthErrorStatus;
import umc.teumteum.server.domain.user.entity.enums.SocialType;
import umc.teumteum.server.domain.auth.exception.AuthHandler;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class NaverOAuthServiceImpl implements NaverOAuthService {

    private final RestTemplate restTemplate;

    // 사용자 정보 조회
    @Override
    public OAuthUserInfo getUserInfoWithAccessToken(String accessToken) {
        String userInfoUrl = "https://openapi.naver.com/v1/nid/me";

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);

        HttpEntity<Void> request = new HttpEntity<>(headers);

        try {
            ResponseEntity<Map> response = restTemplate.exchange(userInfoUrl, HttpMethod.GET, request, Map.class);
            Map<String, Object> body = response.getBody();

            if (body == null || !body.containsKey("response")) {
                throw new AuthHandler(AuthErrorStatus.NAVER_USER_INFO_FAILED);
            }

            Map<String, Object> responseData = (Map<String, Object>) body.get("response");
            String socialId = (String) responseData.get("id");
            String email = (String) responseData.get("email");

            return AuthConverter.toOAuthUserInfo(SocialType.NAVER, socialId, email);

        } catch (Exception e) {
            throw new AuthHandler(AuthErrorStatus.NAVER_USER_INFO_FAILED);
        }
    }
}
