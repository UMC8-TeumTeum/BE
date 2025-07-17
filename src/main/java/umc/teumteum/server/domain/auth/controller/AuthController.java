package umc.teumteum.server.domain.auth.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import umc.teumteum.server.domain.auth.dto.AuthRequestDTO;
import umc.teumteum.server.domain.auth.dto.AuthResponseDTO;
import umc.teumteum.server.domain.auth.service.AuthService;
import umc.teumteum.server.domain.user.entity.enums.SocialType;
import umc.teumteum.server.global.apiPayload.ApiResponse;

@Tag(name = "Auth", description = "인증 관련 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    @Operation(
            summary = "소셜 로그인",
            description = "카카오, 네이버에 대한 소셜 플랫폼을 통한 로그인을 처리합니다."
    )
    @PostMapping(value = "/social-login/{socialType}", produces = "application/json")
    public ApiResponse<AuthResponseDTO.LoginResponse> socialLogin(
            @Parameter(
                    description = "소셜 로그인 타입 (NAVER 또는 KAKAO)",
                    required = true,
                    schema = @Schema(type = "string", allowableValues = {"NAVER", "KAKAO"}, example = "KAKAO")
            )
            @PathVariable("socialType") SocialType socialType,
            @Valid @RequestBody AuthRequestDTO.SocialLoginRequest request
            ) {
        AuthResponseDTO.LoginResponse response = authService.socialLogin(socialType, request);

        return ApiResponse.onSuccess(response);
    }


    @Operation(
            summary = "토큰 재발급",
            description = "리프레시 토큰을 사용하여 새로운 액세스 토큰&리프레시 토큰을 발급받습니다."
    )
    @PostMapping(value = "/refresh", produces = "application/json")
    public ApiResponse<Object> refreshToken(
    ) {
        // TODO: 토큰 재발급 로직 구현
        return null;
    }
}
