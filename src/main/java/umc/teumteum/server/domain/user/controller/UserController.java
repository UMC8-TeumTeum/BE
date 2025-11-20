package umc.teumteum.server.domain.user.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import umc.teumteum.server.domain.user.dto.OnboardingRequestDto;
import umc.teumteum.server.domain.user.dto.OnboardingResponseDto;
import umc.teumteum.server.domain.user.dto.UserRequestDto;
import umc.teumteum.server.domain.user.dto.UserResponseDTO;
import umc.teumteum.server.domain.user.dto.UserSearchResponseDto;
import umc.teumteum.server.domain.user.entity.User;
import umc.teumteum.server.domain.user.entity.enums.Weekday;
import umc.teumteum.server.domain.user.exception.status.UserSuccessStatus;
import umc.teumteum.server.domain.user.service.UserService;
import umc.teumteum.server.global.annotation.CurrentUser;
import umc.teumteum.server.global.apiPayload.ApiResponse;

import java.util.List;


@Tag(name = "User", description = "사용자 관련 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    @Operation(
            summary = "닉네임 사용자 검색",
            description = "닉네임으로 사용자를 검색하여 유저 리스트를 최대 5개 반환합니다."
    )
    @GetMapping("/search")
    public ApiResponse<List<UserSearchResponseDto>> searchByNickname(
            @RequestParam("keyword") String keyword,
            @Parameter(hidden = true) @CurrentUser User user
    ) {
        List<UserSearchResponseDto> results = userService.searchUsersByKeyword(keyword, user.getId());
        return ApiResponse.of(UserSuccessStatus._USER_FOUND, results);
    }

    @Operation(
        summary = "마이페이지 정보 조회",
        description = "사용자의 프로필, 이름, 직업 분야, 아이디 등 마이페이지에 필요한 정보를 조회합니다."
    )
    @GetMapping(value = "/mypage", produces = "application/json")
    public ApiResponse<UserResponseDTO.MyPageDTO> getMypageInfo(
        @Parameter(hidden = true) @CurrentUser User user
    ) {
        UserResponseDTO.MyPageDTO response = userService.getMyPage(user);
        return ApiResponse.of(UserSuccessStatus._USER_FOUND, response);
    }

    @Operation(
            summary = "마이페이지 프로필 수정",
            description = "사용자의 닉네임, 직업 분야, 빈틈 시간 공개 여부를 수정합니다."
    )
    @PatchMapping(value = "/mypage/profile", produces = "application/json")
    public ApiResponse<Void> updateProfile(
            @Valid @RequestBody UserRequestDto.ProfileRequest request,
            @Parameter(hidden = true) @CurrentUser User user
    ){
        userService.updateProfile(request, user);
        return ApiResponse.of(UserSuccessStatus._PROFILE_UPDATED,null);
    }
    @Operation(
            summary = "마이페이지 프로필 수정용 Presigned URL 발급",
            description = "프로필 수정 과정에서 프로필 이미지를 S3에 직접 업로드할 수 있는 Presigned URL을 발급합니다."
    )
    @PostMapping(value = "/mypage/profile-image/presigned-url", produces = "application/json")
    public ApiResponse<OnboardingResponseDto.ProfileImagePresignedUrlResponse> getPresignedImagePresignedUrl(
        HttpServletRequest httpServletRequest,
        @RequestBody @Valid OnboardingRequestDto.ProfileImagePresignedUrlRequest request,
        @CurrentUser @Parameter(hidden = true) User user
    ){
        OnboardingResponseDto.ProfileImagePresignedUrlResponse response =
                userService.generateProfileImagePresignedUrl(httpServletRequest, request, user);
        return ApiResponse.of(UserSuccessStatus._USER_PRESIGNED_URL_ISSUED, response);
    }

    @Operation(
            summary = "마이페이지 프로필 이미지 수정",
            description = "Presigned URL로 업로드된 새 프로필 이미지를 등록합니다. 기존 프로필 이미지는 삭제되고, 새 이미지 파일명이 저장됩니다."
    )
    @PostMapping(value = "/mypage/profile-image", produces = "application/json")
    public ApiResponse<Object> saveProfileImageKey(
                HttpServletRequest httpServletRequest,
                @RequestBody @Valid OnboardingRequestDto.ProfileImageRequest request,
                @CurrentUser @Parameter(hidden = true) User user
    ) {
            userService.saveProfileImage(httpServletRequest, request, user);
            return ApiResponse.of(UserSuccessStatus._PROFILE_IMAGE_UPDATED, null);
    }

    @Operation(
            summary = "마이페이지 프로필 이미지 삭제",
            description = "기존 프로필 이미지를 삭제 후, default 이미지로 수정합니다."
    )
    @DeleteMapping(value = "/mypage/profile-image", produces = "application/json")
    public ApiResponse<Object> deleteProfileImage(
            @CurrentUser @Parameter(hidden = true) User user
    ) {
        userService.deleteProfileImage(user);
        return ApiResponse.of(UserSuccessStatus._PROFILE_IMAGE_DELETED, null);
    }

    @Operation(
            summary = "마이페이지 알림 설정 수정",
            description = "오늘의 일정, 리마인드 알림, 팔로워, 틈 요청에 대해 알림 여부를 수정합니다."
    )
    @PatchMapping(value = "/mypage/alarm", produces = "application/json")
    public ApiResponse<Object> updateAlarm(
            @RequestBody @Valid UserRequestDto.NotificationSettingRequest request,
            @CurrentUser @Parameter(hidden = true) User user
    ) {
        userService.updateAlarm(request, user);
        return ApiResponse.of(UserSuccessStatus._ALARM_STATE_UPDATED, null);
    }

    @Operation(
            summary = "마이페이지 반복일정 조회",
            description = "요일별 반복일정을 조회합니다. query string으로 요일을 입력해주세요."
    )
    @GetMapping(value = "/mypage/routines", produces = "application/json")
    public ApiResponse<List<UserResponseDTO.RoutineDTO>> getRoutines(
            @Parameter(name = "weekday",description = "조회할 요일", example = "MONDAY") @RequestParam("weekday") Weekday weekday,
            @CurrentUser @Parameter(hidden = true) User user
    ){
        List<UserResponseDTO.RoutineDTO> response = userService.getRoutines(weekday, user);
        return ApiResponse.of(UserSuccessStatus._ROUTINE_LOADED,response);
    }

    @Operation(
            summary = "마이페이지 반복일정 삭제",
            description = "특정 반복일정을 삭제합니다."
    )
    @DeleteMapping(value = "/mypage/routines/{routineId}", produces = "application/json")
    public ApiResponse<Object> deleteRoutine(
            @Parameter(name= "routineId", description = "삭제할 Routine Id", example = "123") @PathVariable("routineId") Long routineId
    ){
        userService.deleteRoutine(routineId);
        return ApiResponse.of(UserSuccessStatus._ROUTINE_DELETED, null);
    }

    @Operation(
            summary = "마이페이지 반복일정 추가",
            description = "새로운 반복일정을 등록합니다."
    )
    @PostMapping(value = "/mypage/routines", produces = "application/json")
    public ApiResponse<Object> saveRoutine(
            @RequestBody @Valid OnboardingRequestDto.RoutineDTO request,
            @CurrentUser @Parameter(hidden = true) User user
    ){
        userService.saveRoutine(request, user);
        return ApiResponse.of(UserSuccessStatus._ROUTINE_ADDED,null);
    }

    @Operation(
            summary = "마이페이지 반복일정 수정",
            description = "특정 반복일정을 수정합니다."
    )
    @PatchMapping(value = "/mypage/routines/{routineId}", produces = "application/json")
    public ApiResponse<Object> updateRoutine(
            @Parameter(name= "routineId", description = "수정할 Routine Id", example = "123") @PathVariable("routineId") Long routineId,
            @RequestBody @Valid OnboardingRequestDto.RoutineDTO request,
            @CurrentUser @Parameter(hidden = true) User user
    ){
        userService.updateRoutine(routineId, request, user);
        return ApiResponse.of(UserSuccessStatus._ROUTINE_UPDATED, null);
    }

}