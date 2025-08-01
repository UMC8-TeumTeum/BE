package umc.teumteum.server.domain.friend.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import umc.teumteum.server.domain.friend.dto.*;
import umc.teumteum.server.domain.friend.exception.status.FriendSuccessStatus;
import umc.teumteum.server.domain.friend.service.FriendService;
import umc.teumteum.server.domain.user.entity.User;
import umc.teumteum.server.global.annotation.CurrentUser;
import umc.teumteum.server.global.apiPayload.ApiResponse;
import umc.teumteum.server.global.dto.PagingResponseDto;

import java.util.List;

@Tag(name = "Friend", description = "친구 관련 API")
@RestController
@RequestMapping("/api/friends")
@RequiredArgsConstructor
public class FriendController {

    private final FriendService friendService;

    @Operation(
            summary = "유저 팔로우",
            description = "특정 유저를 팔로우합니다."
    )
    @PostMapping(value = "/{userId}/follow", produces = "application/json")
    public ApiResponse<Object> followUser(
            @Parameter(name = "userId", description = "팔로우할 대상 유저의 ID", example = "1")
            @PathVariable("userId") Long userId,
            @CurrentUser @Parameter(hidden = true) User user
    ) {
        friendService.follow(userId, user);
        return ApiResponse.of(FriendSuccessStatus._FOLLOW_SUCCESS, null);
    }


    @Operation(
            summary = "유저 언팔로우",
            description = "특정 유저에 대한 팔로우를 취소합니다."
    )
    @DeleteMapping(value = "/{userId}/follow", produces = "application/json")
    public ApiResponse<Void> unfollowUser(
            @Parameter(name = "userId", description = "언팔로우할 대상 유저의 ID", example = "1")
            @PathVariable("userId") Long userId
    ) {
        friendService.unfollow(userId);
        return ApiResponse.of(FriendSuccessStatus._UNFOLLOW_SUCCESS, null);
    }


    @Operation(
            summary = "맞팔로우 목록 조회",
            description = "특정 유저의 맞팔로우 목록을 조회합니다."
    )
    @GetMapping(value = "/mutuals", produces = "application/json")
    public ApiResponse<List<FriendMutualResponseDto>> getMyMutualFriends() {
        return ApiResponse.of(FriendSuccessStatus._GET_FRIENDS_SUCCESS, null);
    }


    @Operation(
            summary = "즐겨찾기 설정/해제",
            description = "특정 유저에 대해 즐겨찾기 설정 또는 해제를 합니다."
    )
    @PatchMapping(value = "/{userId}/favorite", consumes = "application/json", produces = "application/json")
    public ApiResponse<FavoriteResponseDto> updateFavorite(
            @Parameter(name = "userId", description = "즐겨찾기를 설정/해제할 대상 유저의 ID", example = "1")
            @PathVariable("userId") Long userId,
            @RequestBody FavoriteRequestDto requestDto
    ) {
        FavoriteResponseDto response = friendService.updateFavorite(userId, requestDto.getIsFavorite());
        return ApiResponse.of(FriendSuccessStatus._FOLLOW_SUCCESS, response);
    }


    @Operation(
            summary = "팔로잉 목록 조회",
            description = "현재 로그인한 사용자가 팔로우한 유저 목록을 조회합니다."
    )
    @GetMapping(value = "/followings", produces = "application/json")
    public ApiResponse<PagingResponseDto<FollowingUserResponseDto>> getFollowingsByUser(
            @Parameter(hidden = true) @CurrentUser User user,
            @Parameter(description = "페이지 번호 (1부터 시작)") @RequestParam(name = "page", defaultValue = "1") int page,
            @Parameter(description = "한 페이지에 포함될 항목 수") @RequestParam(name = "size", defaultValue = "10") int size
    ) {
        PagingResponseDto<FollowingUserResponseDto> response = friendService.getFollowingsByUser(user.getId(), page, size);
        return ApiResponse.of(FriendSuccessStatus._GET_FRIENDS_SUCCESS, response);
    }

    @Operation(
            summary = "팔로워 목록 조회",
            description = "현재 로그인한 사용자를 팔로우한 유저 목록을 조회합니다."
    )
    @GetMapping("/followers")
    public ApiResponse<PagingResponseDto<FollowerUserResponseDto>> getFollowersByUser(
            @Parameter(hidden = true) @CurrentUser User user,
            @Parameter(description = "페이지 번호 (1부터 시작)") @RequestParam(name = "page", defaultValue = "1") int page,
            @Parameter(description = "한 페이지에 포함될 항목 수") @RequestParam(name = "size", defaultValue = "10") int size
    ) {
        PagingResponseDto<FollowerUserResponseDto> response = friendService.getFollowersByUser(user.getId(), page, size);
        return ApiResponse.of(FriendSuccessStatus._GET_FRIENDS_SUCCESS, response);
    }

    @Operation(
            summary = "친구 프로필 조회",
            description = "지정한 친구(userId)의 프로필 정보를 반환합니다."
    )
    @GetMapping(value = "/{userId}/profile", produces = "application/json")
    public ApiResponse<FriendProfileResponseDto> getFriendProfile(
            @Parameter(hidden = true) @CurrentUser User loginUser,
            @Parameter(name = "userId", description = "조회할 친구 ID", example = "2")
            @PathVariable("userId") Long targetUserId
    ) {
        FriendProfileResponseDto response = friendService.getFriendProfile(loginUser.getId(), targetUserId);
        return ApiResponse.of(FriendSuccessStatus._GET_FRIENDS_SUCCESS, response);
    }

    @Operation(
            summary = "친구의 빈틈 시간 조회",
            description = "친구의 스케줄 중에서 includeTeum == true 인 일정들의 시간을 합산하여 반환합니다."
    )
    @GetMapping(value = "/{userId}/teum-time", produces = "application/json")
    public ApiResponse<FriendTeumTimeResponseDto> getFriendTeumTime(
            @Parameter(hidden = true) @CurrentUser User loginUser,
            @Parameter(name = "userId", description = "조회할 친구 ID") @PathVariable("userId") Long targetUserId
    ) {
        FriendTeumTimeResponseDto result = friendService.getFriendTeumTime(loginUser.getId(), targetUserId);
        return ApiResponse.of(FriendSuccessStatus._GET_FRIEND_TEUM_TIME_SUCCESS, result);
    }

    @Operation(
            summary = "최근 공개 투두 2개 조회",
            description = "특정 유저의 최근 공개 투두 2개를 반환합니다."
    )
    @GetMapping(value = "/{userId}/todos/public/recent", produces = "application/json")
    public ApiResponse<List<FriendPublicTodoResponseDto>> getRecentPublicTodos(
            @Parameter(hidden = true) @CurrentUser User loginUser,
            @Parameter(name = "userId", description = "조회할 친구 ID", example = "1")
            @PathVariable("userId") Long targetUserId
    ) {
        List<FriendPublicTodoResponseDto> result = friendService.getRecentPublicTodos(loginUser.getId(), targetUserId);
        return ApiResponse.of(FriendSuccessStatus._GET_FRIEND_PUBLIC_TODO_SUCCESS, result);
    }

    @Operation(
            summary = "특정 날짜의 공개 투두 조회",
            description = "특정 유저의 특정 날짜에 해당하는 모든 공개 투두를 반환합니다."
    )
    @GetMapping(value = "/{userId}/todos/public", produces = "application/json")
    public ApiResponse<List<FriendPublicTodoResponseDto>> getDailyPublicTodos(
            @Parameter(description = "조회할 유저 ID", example = "1")
            @PathVariable("userId") Long userId,

            @Parameter(description = "조회할 날짜 (YYYY-MM-DD)", example = "2024-07-12")
            @RequestParam("date") String date
    ) {
        return ApiResponse.onSuccess(null);
    }

    @Operation(
            summary = "친구의 공개 투두가 있는 날짜(월별) 조회",
            description = "특정 유저의 특정 월에 공개 투두가 존재하는 날짜 목록을 반환합니다."
    )
    @GetMapping(value = "/{userId}/todos/public/calendar", produces = "application/json")
    public ApiResponse<List<String>> getTodoDatesOfMonth(
            @Parameter(hidden = true) @CurrentUser User loginUser,
            @Parameter(name = "userId", description = "조회할 친구 ID", example = "2")
            @PathVariable("userId") Long targetUserId,
            @Parameter(description = "조회할 연월 (YYYY-MM)", example = "2025-05")
            @RequestParam("month") String month
    ) {
        List<String> result = friendService.getTodoDatesOfMonth(loginUser.getId(), targetUserId, month);
        return ApiResponse.of(FriendSuccessStatus._GET_FRIEND_PUBLIC_TODO_SUCCESS, result);    }


}
