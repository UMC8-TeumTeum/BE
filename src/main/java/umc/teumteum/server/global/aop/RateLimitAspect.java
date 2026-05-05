package umc.teumteum.server.global.aop;

import com.auth0.jwt.JWT;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import umc.teumteum.server.domain.auth.dto.AuthRequestDto;
import umc.teumteum.server.global.annotation.RateLimit;
import umc.teumteum.server.global.exception.RateLimitExceededException;
import umc.teumteum.server.global.ratelimit.RateLimitPolicy;
import umc.teumteum.server.global.ratelimit.RateLimiter;
import umc.teumteum.server.global.util.LogHashUtil;

import java.util.Arrays;

@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class RateLimitAspect {

    private final RateLimiter rateLimiter;
    private final HttpServletRequest request;
    private final LogHashUtil logHashUtil;

    @Around("@annotation(rateLimit)")
    public Object applyRateLimit(ProceedingJoinPoint joinPoint, RateLimit rateLimit) throws Throwable {
        String clientIp = resolveClientIp();

        for (RateLimitPolicy policy : rateLimit.policies()) {
            // 1. 정책별 식별자 및 Redis 키 생성
            String identifier = resolveIdentifier(policy, joinPoint, clientIp);
            String key = policy.buildKey(identifier);

            // 2. Rate Limit 검사
            if (!rateLimiter.isAllowed(key, policy)) {
                log.warn("[Rate Limit Exceeded] policy={} id={}",
                        policy.name(),
                        logHashUtil.hashIdentifier(identifier));
                throw new RateLimitExceededException(policy);
            }
        }

        return joinPoint.proceed();
    }

    private String resolveIdentifier(RateLimitPolicy policy, ProceedingJoinPoint joinPoint, String clientIp) {
        return switch (policy) {
            case SOCIAL_LOGIN_SOCIAL_ID -> extractSocialIdFromIdToken(joinPoint);
            case SOCIAL_LOGIN_IP        -> clientIp;
            case REISSUE_USER           -> extractUserIdFromRT(joinPoint);
        };
    }

    // idToken 무검증 decode -> socialId 추출 (Rate Limit 식별자 목적)
    // 네이버는 JWT가 아닌 accessToken -> subject null -> IP fallback
    private String extractSocialIdFromIdToken(ProceedingJoinPoint joinPoint) {
        return Arrays.stream(joinPoint.getArgs())
                .filter(arg -> arg instanceof AuthRequestDto.SocialLoginRequest)
                .map(arg -> (AuthRequestDto.SocialLoginRequest) arg)
                .findFirst()
                .map(req -> {
                    try {
                        String subject = JWT.decode(req.getToken()).getSubject();
                        if (subject == null) {
                            return "IP_" + resolveClientIp();
                        }
                        return subject;
                    } catch (Exception e) {
                        return "INVALID_" + resolveClientIp();
                    }
                })
                .get();
    }

    // RT 무검증 decode -> userId 추출 (Rate Limit 식별자 목적)
    private String extractUserIdFromRT(ProceedingJoinPoint joinPoint) {
        return Arrays.stream(joinPoint.getArgs())
                .filter(arg -> arg instanceof AuthRequestDto.ReissueRequest)
                .map(arg -> (AuthRequestDto.ReissueRequest) arg)
                .findFirst()
                .map(req -> {
                    try {
                        return JWT.decode(req.getRefreshToken()).getSubject();
                    } catch (Exception e) {
                        return "INVALID_" + resolveClientIp();
                    }
                })
                .get();
    }

    // 실제 클라이언트 IP 추출
    private String resolveClientIp() {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (StringUtils.hasText(xForwardedFor)) {
            return xForwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}