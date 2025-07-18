package umc.teumteum.server.domain.user.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

import org.springframework.web.bind.annotation.*;
import umc.teumteum.server.domain.user.dto.PublicTodoResponseDto;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import umc.teumteum.server.domain.user.dto.UserSearchResponseDto;
import umc.teumteum.server.domain.user.exception.status.UserSuccessStatus;
import umc.teumteum.server.domain.user.service.UserService;
import umc.teumteum.server.global.apiPayload.ApiResponse;

import java.util.List;


@Tag(name = "User", description = "사용자 관련 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

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

    @Operation(
            summary = "닉네임 사용자 검색",
            description = "닉네임으로 사용자를 검색하여 유저 리스트를 최대 5개 반환합니다."
    )
    @GetMapping("/search")
    public ApiResponse<List<UserSearchResponseDto>> searchByNickname(
            @RequestParam("keyword") String keyword,
            @RequestParam("userId") Long userId
    ) {
        List<UserSearchResponseDto> results = userService.searchUsersByKeyword(keyword, userId);
        return ApiResponse.of(UserSuccessStatus._USER_FOUND, results);
    }

    @Operation(
            summary = "최근 공개 투두 2개 조회",
            description = "특정 유저의 최근 공개 투두 2개를 반환합니다."
    )
    @GetMapping(value = "/{userId}/todos/public/recent", produces = "application/json")
    public ApiResponse<List<PublicTodoResponseDto>> getRecentPublicTodos(
            @Parameter(description = "공개 투두를 조회할 유저 ID", example = "1")
            @PathVariable("userId") Long userId
    ) {
        return ApiResponse.onSuccess(null);
    }

    @Operation(
            summary = "특정 날짜의 공개 투두 조회",
            description = "특정 유저의 특정 날짜에 해당하는 모든 공개 투두를 반환합니다."
    )
    @GetMapping(value = "/{userId}/todos/public", produces = "application/json")
    public ApiResponse<List<PublicTodoResponseDto>> getDailyPublicTodos(
            @Parameter(description = "조회할 유저 ID", example = "1")
            @PathVariable("userId") Long userId,

            @Parameter(description = "조회할 날짜 (YYYY-MM-DD)", example = "2024-07-12")
            @RequestParam("date") String date
    ) {
        return ApiResponse.onSuccess(null);
    }

    @Operation(
            summary = "공개 투두가 있는 날짜(월별) 조회",
            description = "특정 유저의 특정 월에 공개 투두가 존재하는 날짜 목록을 반환합니다."
    )
    @GetMapping(value = "/{userId}/todos/public/calendar", produces = "application/json")
    public ApiResponse<List<String>> getTodoDatesOfMonth(
            @Parameter(description = "조회할 유저 ID", required = true, example = "1")
            @PathVariable("userId") Long userId,

            @Parameter(description = "조회할 월 (YYYY-MM)", required = true, example = "2024-07")
            @RequestParam("month") String month
    ) {
        return ApiResponse.onSuccess(null);
    }


    @Operation(
        summary = "마이페이지 정보 조회",
        description = "사용자의 프로필, 이름, 직업 분야, 아이디 등 마이페이지에 필요한 정보를 조회합니다."
    )
    @GetMapping(value = "/mypage", produces = "application/json")
    public ApiResponse<Object> getMypageInfo(
    ) {
        // TODO: 마이페이지 화면(프로필,이름,직업분야, 내 아이디) 조회 로직 구현
        return null;
    }


    @Operation(
        summary = "마이페이지 정보 (지금까지 채운 빈틈) 조회",
        description = "사용자가 지금까지 채운 빈틈 시간 정보를 조회합니다."
    )
    @GetMapping(value = "/mypage/teum", produces = "application/json")
    public ApiResponse<Object> getMypageTeum(
    ) {
        // TODO: 마이페이지 "지금까지 채운 빈틈" 조회 로직 구현
        return null;

    }

}