package umc.teumteum.server.global.jwt;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;
import umc.teumteum.server.global.apiPayload.ApiResponse;
import umc.teumteum.server.global.apiPayload.code.status.ErrorStatus;

import java.io.IOException;

@Component
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper = new ObjectMapper();

    // 인증 실패 시, 호출되는 메서드
    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
                         AuthenticationException authException) throws IOException {
        // 1. 예외 타입에 따른 ErrorStatus 결정
        ErrorStatus errorStatus = (ErrorStatus) request.getAttribute("errorStatus");

        // 인증이 필요하지만, AT가 없는 경우
        if (errorStatus == null)
            errorStatus = ErrorStatus._UNAUTHORIZED;

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
}