package umc.teumteum.server.domain.user.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import umc.teumteum.server.global.apiPayload.ApiResponse;


@Tag(name = "User", description = "사용자 관련 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/users")
public class UserController {

    @Operation(
            summary = "온보딩 약관 동의",
            description = "온보딩 과정에서 약관 동의 정보를 저장합니다."
    )
    @PostMapping(value = "/onboarding/terms", produces = "application/json")
    public ApiResponse<Object> saveTerms(
    ) {
        // TODO: 온보딩 약관 동의 저장 로직 구현
        return null;
    }


    @Operation(
            summary = "온보딩 닉네임과 분야/직종 저장",
            description = "온보딩 과정에서 닉네임과 분야/직종 정보를 저장합니다."
    )
    @PostMapping(value = "/onboarding/nickname-job", produces = "application/json")
    public ApiResponse<Object> saveNicknameAndJob(
    ) {
        // TODO: 온보딩 닉네임과 분야/직종 저장 로직 구현
        return null;
    }
}