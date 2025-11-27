package umc.teumteum.server.domain.friend.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import umc.teumteum.server.domain.friend.dto.BlockResponseDto;
import umc.teumteum.server.domain.friend.exception.status.FriendSuccessStatus;
import umc.teumteum.server.domain.friend.service.BlockService;
import umc.teumteum.server.domain.user.entity.User;
import umc.teumteum.server.global.annotation.CurrentUser;
import umc.teumteum.server.global.apiPayload.ApiResponse;
import umc.teumteum.server.global.dto.PagingResponseDto;

@Tag(name = "Block", description = "사용자 차단 관련 API")
@Validated
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

    @Operation(summary = "차단한 유저 목록 조회", description = "내가 차단한 유저 목록을 조회합니다.")
    @GetMapping
    public ApiResponse<PagingResponseDto<BlockResponseDto.BlockedFriend>> getBlockedUsers(
            @CurrentUser @Parameter(hidden = true) User loginUser,
            @Parameter(description = "페이지 번호 (1부터 시작)")
            @RequestParam(defaultValue = "1") @Min(value = 1, message = "page는 1 이상이어야 합니다.") int page,
            @Parameter(description = "페이지 크기")
            @RequestParam(defaultValue = "10") @Min(value = 1, message = "size는 1 이상이어야 합니다.") int size
    ) {
        PagingResponseDto<BlockResponseDto.BlockedFriend> response = blockService.getBlockedUsers(loginUser, page, size);
        return ApiResponse.of(FriendSuccessStatus._GET_BLOCKED_LIST_SUCCESS, response);
    }
}