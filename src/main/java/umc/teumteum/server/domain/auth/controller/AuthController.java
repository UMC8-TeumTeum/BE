package umc.teumteum.server.domain.auth.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import umc.teumteum.server.domain.auth.dto.AuthRequestDto;
import umc.teumteum.server.domain.auth.dto.AuthResponseDto;
import umc.teumteum.server.domain.auth.exception.status.AuthSuccessStatus;
import umc.teumteum.server.domain.auth.service.AuthService;
import umc.teumteum.server.domain.user.entity.User;
import umc.teumteum.server.global.annotation.CurrentUser;
import umc.teumteum.server.global.apiPayload.ApiResponse;

@Tag(name = "Auth", description = "인증 관련 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    @Operation(
            summary = "소셜 로그인",
            description = "카카오, 네이버, 구글 소셜 플랫폼을 통한 로그인을 처리합니다."
    )
    @PostMapping(value = "/social-login/{socialType}", produces = "application/json")
    public ApiResponse<AuthResponseDto.LoginResponse> socialLogin(
            @Parameter(
                    description = "소셜 로그인 타입 (kakao 또는 naver 또는 google)",
                    required = true,
                    schema = @Schema(type = "string", allowableValues = {"kakao", "naver", "google"}, example = "kakao")
            )
            @PathVariable("socialType") String socialType,
            @Valid @RequestBody AuthRequestDto.SocialLoginRequest request
            ) {
        AuthResponseDto.LoginResponse response = authService.socialLogin(socialType, request);

        return ApiResponse.of(AuthSuccessStatus.SOCIAL_LOGIN_SUCCESS, response);
    }


    @Operation(
            summary = "토큰 재발급",
            description = "리프레시 토큰을 사용하여 새로운 액세스 토큰&리프레시 토큰을 발급받습니다."
    )
    @PostMapping(value = "/reissue", produces = "application/json")
    public ApiResponse<AuthResponseDto.ReissueResponse> reissueToken(
            @Valid @RequestBody AuthRequestDto.ReissueRequest request
    ) {
        AuthResponseDto.ReissueResponse response = authService.reissueToken(request);
        return ApiResponse.of(AuthSuccessStatus.TOKEN_REISSUE_SUCCESS, response);
    }


    @Operation(
            summary = "로그아웃",
            description = "AT는 블랙리스트 등록 / RT는 화이트리스트 삭제를 진행합니다."
    )
    @PostMapping(value = "/logout", produces = "application/json")
    public ApiResponse<Object> logout(
            HttpServletRequest httpServletRequest,
            @CurrentUser @Parameter(hidden = true) User user
    ) {
        authService.logout(httpServletRequest, user);
        return ApiResponse.of(AuthSuccessStatus.LOGOUT_SUCCESS, null);
    }
}
