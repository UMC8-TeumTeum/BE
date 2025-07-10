package umc.teumteum.server.domain.auth.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import umc.teumteum.server.global.apiPayload.ApiResponse;

@Tag(name = "Auth", description = "인증 관련 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
public class AuthController {

    @Operation(
            summary = "소셜 로그인",
            description = "카카오, 네이버에 대한 소셜 플랫폼을 통한 로그인을 처리합니다."
    )
    @PostMapping(value = "/social-login", produces = "application/json")
    public ApiResponse<Object> socialLogin(
    ) {
        // TODO: 소셜 로그인 로직 구현
        return null;
    }
}
