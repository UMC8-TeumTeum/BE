package umc.teumteum.server.domain.user.util;

import java.util.Set;

public class ImageConstants {
    // 아미지 형식
    public static final Set<String> ALLOWED_IMAGE_TYPES = Set.of(
            "image/jpeg", "image/png", "image/webp", "image/svg+xml"
    );

    // 기본 이미지
    public static final String DEFAULT_IMAGE = "default.svg";
}
