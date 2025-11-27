package umc.teumteum.server.domain.friend.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import umc.teumteum.server.domain.friend.exception.status.FriendSuccessStatus;
import umc.teumteum.server.domain.friend.service.BlockService;
import umc.teumteum.server.domain.user.entity.User;
import umc.teumteum.server.global.annotation.CurrentUser;
import umc.teumteum.server.global.apiPayload.ApiResponse;

@Tag(name = "Block", description = "사용자 차단 관련 API")
@RestController
@RequestMapping("/api/blocks")
@RequiredArgsConstructor
public class BlockController {

    private final BlockService blockService;

    @Operation(summary = "유저 차단하기", description = "특정 유저를 차단합니다.")
    @PostMapping("/{userId}")
    public ApiResponse<Object> blockUser(
            @CurrentUser @Parameter(hidden = true) User loginUser,
            @Parameter(description = "차단할 유저 ID", example = "1") @PathVariable("userId") Long userId
    ) {
        blockService.blockUser(loginUser, userId);
        return ApiResponse.of(FriendSuccessStatus._BLOCK_SUCCESS, null);
    }

    @Operation(summary = "유저 차단 해제하기", description = "특정 유저의 차단을 해제합니다.")
    @DeleteMapping("/{userId}")
    public ApiResponse<Object> unblockUser(
            @CurrentUser @Parameter(hidden = true) User loginUser,
            @Parameter(description = "차단 해제할 유저 ID", example = "1") @PathVariable("userId") Long userId
    ) {
        blockService.unblockUser(loginUser, userId);
        return ApiResponse.of(FriendSuccessStatus._UNBLOCK_SUCCESS, null);
    }
}