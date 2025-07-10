package umc.teumteum.server.domain.friend.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import umc.teumteum.server.domain.friend.dto.FollowResponseDto;
import umc.teumteum.server.domain.friend.exception.status.FriendSuccessStatus;
import umc.teumteum.server.domain.friend.service.FriendService;
import umc.teumteum.server.global.apiPayload.ApiResponse;

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
    public ApiResponse<FollowResponseDto> followUser(
            @Parameter(
                    name = "userId",
                    required = true
            )
            @PathVariable("userId") Long userId
    ) {
        Long followId = friendService.follow(userId);
        return ApiResponse.of(FriendSuccessStatus._FOLLOW_SUCCESS, new FollowResponseDto(followId));
    }

    @Operation(
            summary = "유저 언팔로우",
            description = "특정 유저에 대한 팔로우를 취소합니다."
    )
    @DeleteMapping(value = "/{userId}/follow", produces = "application/json")
    public ApiResponse<Void> unfollowUser(
            @Parameter(
                    name = "userId",
                    required = true
            )
            @PathVariable("userId") Long userId
    ) {
        friendService.unfollow(userId);
        return ApiResponse.of(FriendSuccessStatus._UNFOLLOW_SUCCESS, null);
    }
}
