package umc.teumteum.server.domain.teum.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import umc.teumteum.server.domain.teum.dto.availability.AvailableTimeRequestDto;
import umc.teumteum.server.domain.teum.dto.availability.AvailableTimeResponseDto;
import umc.teumteum.server.domain.teum.dto.schedule.ScheduledTeumDetailResponseDto;
import umc.teumteum.server.domain.teum.dto.schedule.ScheduledTeumExitResponseDto;
import umc.teumteum.server.domain.teum.dto.schedule.ScheduledTeumResponseDto;
import umc.teumteum.server.domain.teum.dto.shared.SharedTeumResponseDto;
import umc.teumteum.server.domain.teum.dto.teum.*;
import umc.teumteum.server.domain.teum.service.TeumService;
import umc.teumteum.server.global.apiPayload.ApiResponse;

import java.util.List;

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
            @Parameter(name = "parentRequestId", description = "재요청을 생성할 기준이 되는 기존 요청 ID", example = "1")
            @PathVariable("parentRequestId") Long parentRequestId,
            @RequestBody TeumResendRequestDto resendRequestDto
    ) {
        Long id = teumService.createResendRequest(parentRequestId, resendRequestDto);
        return ApiResponse.of(null, new TeumResendResponseDto(id));
    }

    @Operation(
            summary = "내가 받은 틈 요청 조회",
            description = "현재 로그인 사용자가 응답자로 지정된 틈 요청 중, 아직 응답하지 않았고 요청 시간이 지나지 않은 요청 목록을 조회합니다.",
            security = { @SecurityRequirement(name = "BearerAuth") }
    )
    @GetMapping(value = "/request/received", produces = "application/json")
    public ApiResponse<List<TeumReceivedResponseDto>> getReceivedTeumRequests() {
        return ApiResponse.onSuccess(null);
    }

    @Operation(
            summary = "틈 요청 상세 조회",
            description = "응답 ID(responseId)를 기준으로 해당 사용자가 받은 요청 상세 정보를 조회합니다.",
            security = { @SecurityRequirement(name = "BearerAuth") }
    )
    @GetMapping(value = "/{responseId}/request-detail", produces = "application/json")
    public ApiResponse<TeumRequestDetailResponseDto> getRequestDetail(
            @PathVariable("responseId") Long responseId
    ) {
        return ApiResponse.onSuccess(null);
    }

    @Operation(
            summary = "틈 응답 상태 변경",
            description = "응답 ID(responseId)에 해당하는 응답의 상태를 변경합니다. 상태가 'accepted'인 경우 틈 생성 여부를 판단하여 반환합니다.",
            security = { @SecurityRequirement(name = "BearerAuth") }
    )
    @PatchMapping(value = "/response/{responseId}/status", consumes = "application/json", produces = "application/json")
    public ApiResponse<TeumStatusUpdateResponseDto> updateResponseStatus(
            @Parameter(name = "responseId", description = "상태를 변경할 응답 ID", example = "1")
            @PathVariable("responseId") Long responseId,
            @RequestBody TeumStatusUpdateRequestDto requestDto
    ) {
        return ApiResponse.onSuccess(null);
    }

    @Operation(
            summary = "약속된 틈 조회",
            description = "사용자가 참여 중인 틈 중, 지정한 연/월에 해당하는 틈 목록을 조회합니다.",
            security = { @SecurityRequirement(name = "BearerAuth") }
    )
    @GetMapping(value = "/scheduled", produces = "application/json")
    public ApiResponse<List<ScheduledTeumResponseDto>> getScheduledTeums(
            @Parameter(name = "year", description = "조회할 연도", example = "2025")
            @RequestParam("year") int year,
            @Parameter(name = "month", description = "조회할 월", example = "7")
            @RequestParam("month") int month
    ) {
        return ApiResponse.onSuccess(null);
    }

    @Operation(
            summary = "약속된 틈 상세 조회",
            description = "사용자가 참여 중인 틈(teumId)에 대한 상세 정보를 조회합니다. 과거 여부도 함께 반환됩니다.",
            security = { @SecurityRequirement(name = "BearerAuth") }
    )
    @GetMapping(value = "/scheduled/{teumId}", produces = "application/json")
    public ApiResponse<ScheduledTeumDetailResponseDto> getTeumDetail(
            @Parameter(name = "teumId", description = "상세 정보를 조회할 틈 ID", example = "1")
            @PathVariable("teumId") Long teumId
    ) {
        return ApiResponse.onSuccess(null);
    }

    @Operation(
            summary = "약속된 틈 나가기",
            description = "현재 로그인한 사용자가 참여 중인 틈(teumId)에서 나갑니다. 마지막 참여자가 나갈 경우 틈은 cancelled 상태로 변경됩니다.",
            security = { @SecurityRequirement(name = "BearerAuth") }
    )
    @DeleteMapping(value = "/scheduled/{teumId}/exit", produces = "application/json")
    public ApiResponse<ScheduledTeumExitResponseDto> exitScheduledTeum(
            @Parameter(name = "teumId", description = "나갈 틈 ID", example = "1")
            @PathVariable("teumId") Long teumId
    ) {
        return ApiResponse.onSuccess(null);
    }

    @Operation(
            summary = "공통 가능한 시간대 조회",
            description = "지정된 사용자들(memberIds)의 특정 날짜에 대해 공통으로 가능한 시간대를 반환합니다.",
            security = { @SecurityRequirement(name = "BearerAuth") }
    )
    @PostMapping(value = "/availability", consumes = "application/json", produces = "application/json")
    public ApiResponse<AvailableTimeResponseDto> getAvailableTime(
            @RequestBody AvailableTimeRequestDto requestDto
    ) {
        return ApiResponse.onSuccess(null);
    }

    @Operation(
            summary = "함께한 틈 시간 조회",
            description = "로그인한 사용자와 지정된 친구가 함께 참여한 틈의 횟수와 누적 시간을 분 단위로 반환합니다.",
            security = { @SecurityRequirement(name = "BearerAuth") }
    )
    @GetMapping(value = "/shared-time/{userId}", produces = "application/json")
    public ApiResponse<SharedTeumResponseDto> getSharedTeumStats(
            @Parameter(name = "userId", description = "함께한 틈 정보를 조회할 친구 ID", example = "1")
            @PathVariable("userId") Long userId
    ) {
        return ApiResponse.onSuccess(null);
    }


}
