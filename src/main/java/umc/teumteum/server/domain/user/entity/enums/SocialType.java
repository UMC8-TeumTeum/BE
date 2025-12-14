package umc.teumteum.server.domain.user.entity.enums;

import umc.teumteum.server.domain.auth.exception.AuthException;
import umc.teumteum.server.domain.auth.exception.status.AuthErrorStatus;

public enum SocialType {
    KAKAO,      // 카카오
    NAVER,      // 네이버
    GOOGLE,     // 구글
    ;

    public static SocialType from(String type) {
        // 소문자 확인
        if (!type.equals(type.toLowerCase())) {
            throw new AuthException(AuthErrorStatus.INVALID_SOCIAL_TYPE);
        }

        // enum 변환
        try {
            return valueOf(type.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new AuthException(AuthErrorStatus.INVALID_SOCIAL_TYPE);
        }
    }
}