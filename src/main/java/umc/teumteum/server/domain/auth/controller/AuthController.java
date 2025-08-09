package umc.teumteum.server.domain.auth.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.*;
import umc.teumteum.server.domain.auth.dto.AuthRequestDTO;
import umc.teumteum.server.domain.auth.dto.AuthResponseDTO;
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
            description = "카카오, 네이버에 대한 소셜 플랫폼을 통한 로그인을 처리합니다."
    )
    @PostMapping(value = "/social-login/{socialType}", produces = "application/json")
    public ApiResponse<AuthResponseDTO.LoginResponse> socialLogin(
            @Parameter(
                    description = "소셜 로그인 타입 (naver 또는 kakao)",
                    required = true,
                    schema = @Schema(type = "string", allowableValues = {"naver", "kakao"}, example = "kakao")
            )
            @PathVariable("socialType") String socialType,
            @Valid @RequestBody AuthRequestDTO.SocialLoginRequest request
            ) {
        AuthResponseDTO.LoginResponse response = authService.socialLogin(socialType, request);

        return ApiResponse.of(AuthSuccessStatus.SOCIAL_LOGIN_SUCCESS, response);
    }


    @Operation(
            summary = "토큰 재발급",
            description = "리프레시 토큰을 사용하여 새로운 액세스 토큰&리프레시 토큰을 발급받습니다."
    )
    @PostMapping(value = "/reissue", produces = "application/json")
    public ApiResponse<Object> reissueToken(
            @Valid @RequestBody AuthRequestDTO.ReissueRequest request
    ) {
        AuthResponseDTO.ReissueResponse response = authService.reissueToken(request);
        return ApiResponse.of(AuthSuccessStatus.TOKEN_REISSUE_SUCCESS, response);
    }


//    @GetMapping(value = "/test/jwt", produces = "application/json")
//    public ApiResponse<Long> getUser(
//            @CurrentUser @Parameter(hidden = true) User user
//    ) {
//        return ApiResponse.onSuccess(user.getId());
//    }
}
