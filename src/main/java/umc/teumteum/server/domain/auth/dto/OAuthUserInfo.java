package umc.teumteum.server.domain.auth.dto;

import lombok.Builder;
import lombok.Getter;
import umc.teumteum.server.domain.user.entity.enums.SocialType;

@Getter
@Builder
public class OAuthUserInfo {

    private SocialType socialType;  // 소셜 로그인 타입 (kakao, naver)
    private String socialId;        // 소셜 로그인 시의 고유 ID
    private String email;           // 소셜에서의 사용자 이메일
}
