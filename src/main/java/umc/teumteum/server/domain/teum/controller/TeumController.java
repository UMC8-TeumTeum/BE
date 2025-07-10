package umc.teumteum.server.domain.teum.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import umc.teumteum.server.domain.teum.dto.TeumRequestDto;
import umc.teumteum.server.domain.teum.dto.TeumResendRequestDto;
import umc.teumteum.server.domain.teum.dto.TeumResendResponseDto;
import umc.teumteum.server.domain.teum.dto.TeumResponseDto;
import umc.teumteum.server.domain.teum.service.TeumService;
import umc.teumteum.server.global.apiPayload.ApiResponse;

@Tag(name = "Teum", description = "틈 관련 API")
@RestController
@RequestMapping("/api/teums")
@RequiredArgsConstructor
public class TeumController {

    private final TeumService teumService;

    @Operation(
            summary = "틈 요청",
            description = "틈 요청을 전송합니다.",
            security = { @SecurityRequirement(name = "BearerAuth") }
    )
    @PostMapping(value = "/request", consumes = "application/json", produces = "application/json")
    public ApiResponse<TeumResponseDto> createTeumRequest(@RequestBody TeumRequestDto requestDto) {
        Long id = teumService.createRequest(requestDto);
        return ApiResponse.of(null, new TeumResponseDto(id));
    }

    @Operation(
            summary = "틈 재요청",
            description = "특정 요청을 기반으로 재요청(시간 제안)을 생성합니다.",
            security = { @SecurityRequirement(name = "BearerAuth") }
    )
    @PostMapping(value = "/request/{parentRequestId}/resend", consumes = "application/json", produces = "application/json")
    public ApiResponse<TeumResendResponseDto> createResendRequest(
            @PathVariable("parentRequestId") Long parentRequestId,
            @RequestBody TeumResendRequestDto resendRequestDto
    ) {
        Long id = teumService.createResendRequest(parentRequestId, resendRequestDto);
        return ApiResponse.of(null, new TeumResendResponseDto(id));
    }

}
