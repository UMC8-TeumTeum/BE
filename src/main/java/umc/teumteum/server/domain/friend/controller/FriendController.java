package umc.teumteum.server.domain.friend.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import umc.teumteum.server.domain.friend.dto.*;
import umc.teumteum.server.domain.friend.exception.status.FriendSuccessStatus;
import umc.teumteum.server.domain.friend.service.FriendService;
import umc.teumteum.server.global.apiPayload.ApiResponse;

import java.util.List;

@Tag(name = "Friend", description = "친구 관련 API")
@RestController
@RequestMapping("/api/friends")
@RequiredArgsConstructor
public class FriendController {

    private final FriendService friendService;

    @Operation(
            summary = "유저 팔로우",
            description = "특정 유저를 팔로우합니다.",
            security = { @SecurityRequirement(name = "BearerAuth") }
    )
    @PostMapping(value = "/{userId}/follow", produces = "application/json")
    public ApiResponse<FollowResponseDto> followUser(
            @Parameter(name = "userId", description = "팔로우할 대상 유저의 ID", example = "1")
            @PathVariable("userId") Long userId
    ) {
        Long followId = friendService.follow(userId);
        return ApiResponse.of(FriendSuccessStatus._FOLLOW_SUCCESS, new FollowResponseDto(followId));
    }

    @Operation(
            summary = "유저 언팔로우",
            description = "특정 유저에 대한 팔로우를 취소합니다.",
            security = { @SecurityRequirement(name = "BearerAuth") }
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
            description = "특정 유저의 맞팔로우 목록을 조회합니다.",
            security = { @SecurityRequirement(name = "BearerAuth") }
    )
    @GetMapping(value = "/mutuals", produces = "application/json")
    public ApiResponse<List<FriendMutualResponseDto>> getMyMutualFriends() {
        return ApiResponse.of(FriendSuccessStatus._GET_FRIENDS_SUCCESS, null);
    }

    @Operation(
            summary = "즐겨찾기 설정/해제",
            description = "특정 유저에 대해 즐겨찾기 설정 또는 해제를 합니다.",
            security = { @SecurityRequirement(name = "BearerAuth") }
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
            description = "내가 팔로우한 유저 목록을 조회합니다.",
            security = { @SecurityRequirement(name = "BearerAuth") }
    )
    @GetMapping(value = "/followings", produces = "application/json")
    public ApiResponse<List<FollowingUserResponseDto>> getFollowings() {
        List<FollowingUserResponseDto> response = friendService.getFollowings();
        return ApiResponse.of(FriendSuccessStatus._GET_FRIENDS_SUCCESS, response);
    }

    @Operation(
            summary = "팔로워 목록 조회",
            description = "나를 팔로우한 유저 목록을 조회합니다.",
            security = { @SecurityRequirement(name = "BearerAuth") }
    )
    @GetMapping(value = "/followers", produces = "application/json")
    public ApiResponse<List<FollowerUserResponseDto>> getFollowers() {
        List<FollowerUserResponseDto> response = friendService.getFollowers();
        return ApiResponse.of(FriendSuccessStatus._GET_FRIENDS_SUCCESS, response);
    }

}
