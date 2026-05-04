package umc.teumteum.server.global.ratelimit;

public enum RateLimitPolicy {

    // social-login: socialId 핵심 키 (IP 로테이션, UA 위변조 공격 대응)
    SOCIAL_LOGIN_SOCIAL_ID(
            "RATE_LIMIT:SOCIAL_LOGIN:SOCIAL_ID:%s",
            10 * 60 * 1000L,
            10
    ),

    // social-login: IP 보조 키 (idToken 없이 무차별 시도하는 공격 대응)
    SOCIAL_LOGIN_IP(
            "RATE_LIMIT:SOCIAL_LOGIN:IP:%s",
            10 * 60 * 1000L,
            50
    ),

    // reissue: userId 핵심 키 (IP 로테이션 공격 대응)
    REISSUE_USER(
            "RATE_LIMIT:REISSUE:USER:%s",
            10 * 60 * 1000L,
            15
    );

    private final String keyTemplate;
    private final long windowMs;
    private final int limit;

    RateLimitPolicy(String keyTemplate, long windowMs, int limit) {
        this.keyTemplate = keyTemplate;
        this.windowMs = windowMs;
        this.limit = limit;
    }

    public String buildKey(String identifier) {
        return String.format(keyTemplate, identifier);
    }

    public long getWindowMs() { return windowMs; }
    public int getLimit() { return limit; }
}