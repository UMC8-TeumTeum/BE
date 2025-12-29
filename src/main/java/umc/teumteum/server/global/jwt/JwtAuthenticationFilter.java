package umc.teumteum.server.global.jwt;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.security.SecurityException;
import jakarta.annotation.Resource;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import umc.teumteum.server.global.apiPayload.code.status.ErrorStatus;
import umc.teumteum.server.global.exception.InvalidTokenTypeException;
import umc.teumteum.server.global.exception.TokenBlacklistException;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtProvider jwtProvider;

    @Resource(name = "atBlacklistRedisTemplate")
    private RedisTemplate<String, String> atBlacklistRedisTemplate;

    // JWT 토큰 검증 및 인증 처리
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        try {
            // 1. Authorization 헤더에서 JWT 토큰 추출
            String token = jwtProvider.resolveToken(request);

            if (token != null) {
                // 2. 액세스 토큰 유효성 검증
                jwtProvider.validateAccessToken(token);

                // 3. 토큰에서 사용자ID & 세션ID 추출
                String userId = jwtProvider.getUserIdFromToken(token);
                String sessionId = jwtProvider.getSessionIdFromToken(token);
                String userRole = jwtProvider.getUserRoleFromToken(token);

                // 4. Redis 블랙리스트 확인 (있으면 -> 재로그인 필요)
                String blacklistKey = String.format("AT_BLACKLIST:%s:%s", userId, sessionId);
                if (atBlacklistRedisTemplate.hasKey(blacklistKey)) {
                    log.error("블랙리스트된 토큰 접근 시도 - userId: {}, sessionId: {}", userId, sessionId);
                    throw new TokenBlacklistException("블랙리스트된 토큰입니다.");
                }

                // 5. Authentication 객체 생성
                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(userId, null, List.of(new SimpleGrantedAuthority(userRole)));

                // 6. SecurityContext에 인증 정보 설정
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        } catch (Exception e) {
            // 발생한 예외에 따라서 적절한 ErrorStatus 지정 (AuthenticationEntryPoint에서 처리)
            ErrorStatus errorStatus = determineErrorStatus(e);
            request.setAttribute("errorStatus", errorStatus);
        }

        filterChain.doFilter(request, response);
    }


    // 적절한 ErrorStatus 결정
    private ErrorStatus determineErrorStatus(Exception ex) {
        return switch (ex) {
            // JWT 토큰 검증 관련 예외
            case SecurityException sec -> ErrorStatus.INVALID_JWT_SIGNATURE;
            case MalformedJwtException mal -> ErrorStatus.MALFORMED_JWT_TOKEN;
            case ExpiredJwtException exp -> ErrorStatus.EXPIRED_JWT_TOKEN;
            case UnsupportedJwtException unsup -> ErrorStatus.UNSUPPORTED_JWT_TOKEN;
            case IllegalArgumentException illegal -> ErrorStatus.EMPTY_JWT_CLAIMS;

            // 유효하지 않은 토큰 타입(=refresh)에 대한 예외 (커스텀)
            case InvalidTokenTypeException invalidType -> ErrorStatus.INVALID_TOKEN_TYPE;

            // 유효하지 않은 AT 예외 (커스텀)
            case TokenBlacklistException blacklist -> ErrorStatus.ACCESS_TOKEN_BLACKLISTED;

            default -> ErrorStatus._UNAUTHORIZED;
        };
    }
}
