package umc.teumteum.server.domain.user.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import umc.teumteum.server.domain.user.dto.UserResponseDTO;
import umc.teumteum.server.domain.user.dto.UserSearchResponseDto;
import umc.teumteum.server.domain.user.entity.User;
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


}