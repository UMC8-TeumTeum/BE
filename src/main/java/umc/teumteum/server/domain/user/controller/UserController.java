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


    @Operation(
            summary = "온보딩 프로필 이미지 저장",
            description = "온보딩 과정에서 S3에 업로드되고 난 뒤의 프로필 이미지 키를 저장합니다."
    )
    @PostMapping(value = "/onboarding/profile-image", produces = "application/json")
    public ApiResponse<Object> saveProfileImageKey(
    ) {
        // TODO: 온보딩 프로필 이미지 키 저장 로직 구현
        return null;
    }


    @Operation(
            summary = "온보딩 요일별 반복 일정 저장",
            description = "온보딩 과정에서 요일별 반복 일정을 저장합니다."
    )
    @PostMapping(value = "/onboarding/routine", produces = "application/json")
    public ApiResponse<Object> saveRoutine(
    ) {
        // TODO: 온보딩 루틴 저장 로직 구현
        return null;
    }


    @Operation(
            summary = "온보딩 수면 패턴 저장",
            description = "온보딩 과정에서 수면 패턴(취침시간/기상시간)을 저장합니다."
    )
    @PostMapping(value = "/onboarding/sleep-pattern", produces = "application/json")
    public ApiResponse<Object> saveSleepPattern(
    ) {
        // TODO: 온보딩 수면 패턴 저장 로직 구현
        return null;
    }


    @Operation(
            summary = "온보딩 리마인드 알림 설정 저장",
            description = "온보딩 과정에서 리마인드 알림 시간 설정(1분 전/3분 전/5분 전/10분 전/30분 전)을 저장합니다."
    )
    @PostMapping(value = "/onboarding/reminder", produces = "application/json")
    public ApiResponse<Object> saveReminder(
    ) {
        // TODO: 온보딩 리마인드 알림 설정 저장 로직 구현
        return null;
    }
}