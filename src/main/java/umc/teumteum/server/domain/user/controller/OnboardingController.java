package umc.teumteum.server.domain.user.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import umc.teumteum.server.domain.user.dto.OnboardingRequestDto;
import umc.teumteum.server.domain.user.dto.OnboardingResponseDto;
import umc.teumteum.server.domain.user.entity.User;
import umc.teumteum.server.domain.user.exception.status.UserSuccessStatus;
import umc.teumteum.server.domain.user.service.OnboardingService;
import umc.teumteum.server.domain.user.service.UserService;
import umc.teumteum.server.global.annotation.CurrentUser;
import umc.teumteum.server.global.apiPayload.ApiResponse;


@Tag(name = "User", description = "사용자 관련 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/users")
public class OnboardingController {

    private final UserService userService;
    private final OnboardingService onboardingService;

    @Operation(
            summary = "온보딩 약관 동의",
            description = "온보딩 과정에서 약관 동의 정보를 등록합니다."
    )
    @PostMapping(value = "/onboarding/agreements", produces = "application/json")
    public ApiResponse<Object> saveAgreements(
            @RequestBody @Valid OnboardingRequestDto.AgreeRequest request,
            @CurrentUser @Parameter(hidden = true) User user
            ) {
        onboardingService.saveAgreements(request, user);
        return ApiResponse.of(UserSuccessStatus.AGREEMENT_SAVED, null);
    }


    @Operation(
            summary = "온보딩 닉네임과 분야/직종 등록",
            description = "온보딩 과정에서 닉네임과 분야/직종 정보를 등록합니다."
    )
    @PostMapping(value = "/onboarding/nickname-job", produces = "application/json")
    public ApiResponse<Object> saveNicknameAndJob(
            @RequestBody @Valid OnboardingRequestDto.NicknameJobRequest request,
            @CurrentUser @Parameter(hidden = true) User user
    ) {
        onboardingService.saveNicknameAndJob(request, user);
        return ApiResponse.of(UserSuccessStatus.NICKNAME_JOB_SAVED, null);
    }


    @Operation(
            summary = "온보딩 프로필 이미지 업로드용 Presigned URL 발급",
            description = "온보딩 과정에서 프로필 이미지를 S3에 직접 업로드할 수 있는 Presigned URL을 발급합니다."
    )
    @PostMapping(value = "/onboarding/profile-image/presigned-url", produces = "application/json")
    public ApiResponse<OnboardingResponseDto.ProfileImagePresignedUrlResponse> getProfileImagePresignedUrl(
            @RequestBody @Valid OnboardingRequestDto.ProfileImagePresignedUrlRequest request,
            @CurrentUser @Parameter(hidden = true) User user
    ) {
        OnboardingResponseDto.ProfileImagePresignedUrlResponse response =
                onboardingService.generateProfileImagePresignedUrl(request, user);
        return ApiResponse.of(UserSuccessStatus.PRESIGNED_URL_ISSUED, response);
    }


    @Operation(
            summary = "온보딩 프로필 이미지 등록",
            description = "온보딩 과정에서 S3에 업로드된 프로필 이미지의 파일명를 등록합니다."
    )
    @PostMapping(value = "/onboarding/profile-image", produces = "application/json")
    public ApiResponse<Object> saveProfileImageKey(
    ) {
        // TODO: 온보딩 프로필 이미지 파일명 저장 로직 구현
        return null;
    }


    @Operation(
            summary = "온보딩 수면 패턴 등록",
            description = "온보딩 과정에서 수면 패턴(취침시간/기상시간)을 등록합니다."
    )
    @PostMapping(value = "/onboarding/sleep-pattern", produces = "application/json")
    public ApiResponse<Object> saveSleepPattern(
            @RequestBody @Valid OnboardingRequestDto.SleepPatternRequest request,
            @CurrentUser @Parameter(hidden = true) User user
    ) {
        onboardingService.saveSleepPattern(request, user);
        return ApiResponse.of(UserSuccessStatus.SLEEP_PATTERN_SAVED, null);
    }


    @Operation(
            summary = "온보딩 요일별 반복 일정 등록",
            description = "온보딩 과정에서 요일별 반복 일정을 등록합니다."
    )
    @PostMapping(value = "/onboarding/routines", produces = "application/json")
    public ApiResponse<Object> saveRoutines(
            @RequestBody @Valid OnboardingRequestDto.RoutineListRequest request,
            @CurrentUser @Parameter(hidden = true) User user
    ) {
        onboardingService.saveRoutines(request, user);
        return ApiResponse.of(UserSuccessStatus.ROUTINE_SAVED, null);
    }


    @Operation(
            summary = "온보딩 리마인드 알림 설정 등록",
            description = "온보딩 과정에서 리마인드 알림 시간 설정(1분 전/3분 전/5분 전/10분 전/30분 전)을 등록합니다."
    )
    @PostMapping(value = "/onboarding/reminders", produces = "application/json")
    public ApiResponse<Object> saveReminder(
    ) {
        // TODO: 온보딩 리마인드 알림 설정 저장 로직 구현
        return null;
    }
}