package umc.teumteum.server.domain.auth.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.*;
import umc.teumteum.server.domain.auth.dto.AuthResponseDto;
import umc.teumteum.server.domain.auth.exception.status.AuthSuccessStatus;
import umc.teumteum.server.domain.auth.service.AuthService;
import umc.teumteum.server.global.apiPayload.ApiResponse;

@Profile("dev")
@Tag(name = "Auth", description = "인증 관련 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
public class DevAuthController {

    private final AuthService authService;

    @Operation(
            summary = "개발용 액세스 토큰 발급",
            description = "개발 진행 과정에서의 테스트를 위한 액세스 토큰을 발급합니다."
    )
    @PostMapping(value = "/dev-token", produces = "application/json")
    public ApiResponse<AuthResponseDto.DevTokenResponse> generateDevToken() {
        AuthResponseDto.DevTokenResponse response = authService.generateDevTokens();

        return ApiResponse.of(AuthSuccessStatus.DEV_TOKEN_ISSUED, response);
    }
}
