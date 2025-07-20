package umc.teumteum.server.global.jwt;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;
import umc.teumteum.server.global.apiPayload.ApiResponse;
import umc.teumteum.server.global.apiPayload.code.status.ErrorStatus;
import umc.teumteum.server.global.exception.InvalidTokenTypeException;

import java.io.IOException;

@Component
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper = new ObjectMapper();

    // 인증 실패 시 호출되는 메서드
    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
                         AuthenticationException authException) throws IOException {

        // 1. 예외 타입에 따른 ErrorStatus 결정
        ErrorStatus errorStatus = determineErrorStatus(authException);

        // 2. HTTP 응답 설정
        response.setStatus(errorStatus.getHttpStatus().value());
        response.setContentType("application/json;charset=UTF-8");

        // 3. API 응답
        ApiResponse<Object> apiResponse = ApiResponse.onFailure(
                errorStatus.getCode(),
                errorStatus.getMessage(),
                null
        );

        response.getWriter().write(objectMapper.writeValueAsString(apiResponse));
    }

    // 적절한 ErrorStatus 반환
    private ErrorStatus determineErrorStatus(AuthenticationException ex) {

        // 1. JWT 토큰 검증 관련 예외
        // cause : 실제 원인이 되는 예외
        Throwable cause = ex.getCause();

        if (cause instanceof SecurityException) {
            return ErrorStatus.INVALID_JWT_SIGNATURE;
        } else if (cause instanceof MalformedJwtException) {
            return ErrorStatus.MALFORMED_JWT_TOKEN;
        } else if (cause instanceof ExpiredJwtException) {
            return ErrorStatus.EXPIRED_JWT_TOKEN;
        } else if (cause instanceof UnsupportedJwtException) {
            return ErrorStatus.UNSUPPORTED_JWT_TOKEN;
        } else if (cause instanceof IllegalArgumentException) {
            return ErrorStatus.EMPTY_JWT_CLAIMS;
        }

        // 유효하지 않은(=refresh) 토큰 타입에 대한 예외 (커스텀)
        if (ex instanceof InvalidTokenTypeException) {
            return ErrorStatus.INVALID_TOKEN_TYPE;
        }

        // 2. 사용자 인증 과정 예외
        if (ex instanceof DisabledException) {
            return ErrorStatus.ACCOUNT_DISABLED;
        }
        if (ex instanceof UsernameNotFoundException) {
            return ErrorStatus.USER_NOT_FOUND;
        }

        return ErrorStatus._UNAUTHORIZED;
    }
}