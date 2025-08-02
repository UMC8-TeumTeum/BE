package umc.teumteum.server.domain.teum.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;
import umc.teumteum.server.domain.teum.dto.availability.AvailableTimeRequestDto;
import umc.teumteum.server.domain.teum.dto.availability.AvailableTimeResponseDto;
import umc.teumteum.server.domain.teum.dto.schedule.ScheduledTeumCancelResponseDto;
import umc.teumteum.server.domain.teum.dto.schedule.ScheduledTeumDetailResponseDto;
import umc.teumteum.server.domain.teum.dto.schedule.ScheduledTeumResponseDto;
import umc.teumteum.server.domain.teum.dto.shared.SharedTeumListResponseDto;
import umc.teumteum.server.domain.teum.dto.shared.SharedTeumTimeResponseDto;
import umc.teumteum.server.domain.teum.dto.teum.*;
import umc.teumteum.server.domain.teum.exception.status.TeumSuccessStatus;
import umc.teumteum.server.domain.teum.service.TeumService;
import umc.teumteum.server.domain.user.entity.User;
import umc.teumteum.server.global.annotation.CurrentUser;
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
            description = "틈 요청을 전송합니다."
    )
    @PostMapping(value = "/request", consumes = "application/json", produces = "application/json")
    public ApiResponse<Long> createTeumRequest(
            @RequestBody @Valid TeumRequestDto requestDto,
            @CurrentUser @Parameter(hidden = true) User user
    ) {
        Long id = teumService.createRequest(requestDto, user);
        return ApiResponse.of(TeumSuccessStatus._TEUM_REQUEST_CREATED, id);
    }

    @Operation(
            summary = "틈 재요청",
            description = "특정 요청을 기반으로 재요청(시간 제안)을 생성합니다."
    )
    @PostMapping(value = "/request/{parentRequestId}/resend", consumes = "application/json", produces = "application/json")
    public ApiResponse<TeumResendResponseDto> createResendRequest(
            @Parameter(name = "parentRequestId", description = "재요청을 생성할 기준이 되는 기존 요청 ID", example = "1")
            @PathVariable("parentRequestId") Long parentRequestId,
            @RequestBody TeumResendRequestDto resendRequestDto,
            @CurrentUser @Parameter(hidden = true) User user
    ) {
        Long id = teumService.createResendRequest(parentRequestId, resendRequestDto, user);
        return ApiResponse.of(TeumSuccessStatus._TEUM_REQUEST_CREATED, new TeumResendResponseDto(id));
    }

    @Operation(
            summary = "틈 요청 조회",
            description = "현재 로그인 사용자가 응답자로 지정된 틈 요청 중, 아직 응답하지 않았고 요청 시간이 지나지 않은 요청 목록을 조회합니다."
    )
    @GetMapping("/request/received")
    public ApiResponse<Page<TeumReceivedResponseDto>> getReceivedTeumRequests(
            @Parameter(hidden = true) @CurrentUser User user,
            @Parameter(description = "페이지 번호 (1부터 시작)") @RequestParam(name = "page", defaultValue = "1") int page,
            @Parameter(description = "한 페이지에 포함될 항목 수") @RequestParam(name = "size", defaultValue = "10") int size
    ) {
        Page<TeumReceivedResponseDto> responses = teumService.getReceivedRequests(user.getId(), page, size);
        return ApiResponse.of(TeumSuccessStatus._TEUM_RECEIVED_LIST_LOADED, responses);
    }

    @Operation(
            summary = "틈 요청 읽기 처리",
            description = "응답 ID(responseId)와 사용자 ID(userId)를 기준으로 틈 요청을 읽음 처리합니다."
    )
    @PatchMapping(value = "/request/{responseId}/read", produces = "application/json")
    public ApiResponse<Long> updateReadStatus(
            @Parameter(name = "responseId", description = "응답 ID", example = "1")
            @PathVariable("responseId") Long responseId,
            @Parameter(hidden = true) @CurrentUser User user
    ) {
        Long id = teumService.updateReadStatus(responseId, user.getId());
        return ApiResponse.of(TeumSuccessStatus._TEUM_READ_SUCCESS, id);
    }

    @Operation(
            summary = "틈 응답 상태 변경",
            description = "응답 ID(responseId)에 해당하는 응답의 상태를 변경합니다. 상태가 'accepted'인 경우 틈 생성 여부를 판단하여 반환합니다."
    )
    @PatchMapping("/response/{responseId}/status")
    public ApiResponse<TeumStatusUpdateResponseDto> updateResponseStatus(
            @Parameter(name = "responseId", description = "응답 ID", example = "1")
            @PathVariable("responseId") Long responseId,
            @Parameter(hidden = true) @CurrentUser User user,
            @RequestBody TeumStatusUpdateRequestDto requestDto
    ) {
        TeumStatusUpdateResponseDto result = teumService.updateResponseStatus(responseId, user.getId(), requestDto);
        return ApiResponse.of(TeumSuccessStatus._TEUM_STATUS_UPDATED, result);
    }

    @Operation(
            summary = "틈 요청 날짜 리스트 조회",
            description = "지정한 월에 틈 요청이 있는 날짜만 리스트로 반환합니다."
    )
    @GetMapping(value = "/requests/calendar", produces = "application/json")
    public ApiResponse<List<String>> getTeumRequestsOfMonth(
            @Parameter(description = "조회할 연월 (YYYY-MM)", example = "2025-05")
            @RequestParam("month") String month,
            @CurrentUser @Parameter(hidden = true) User user
    ) {
        List<String> dates = teumService.getTeumRequestsOfMonth(user.getId(), month);
        return ApiResponse.of(TeumSuccessStatus._TEUM_CALENDAR_LOADED, dates);
    }

    @Operation(
            summary = "특정 날짜의 틈 요청 조회",
            description = "지정한 날짜에 해당하는 틈 요청 목록을 조회합니다."
    )
    @GetMapping(value = "/requests", produces = "application/json")
    public ApiResponse<List<TeumRequestResponseDto>> getTeumRequestsByDate(
            @Parameter(description = "조회할 날짜 (YYYY-MM-DD)", example = "2025-05-02")
            @RequestParam("date") String date
    ) {
        return ApiResponse.onSuccess(null);
    }

    @Operation(
            summary = "약속된 틈 날짜 리스트 조회",
            description = "사용자가 참여 중인 틈 중, 지정한 월에 약속된 틈이 있는 날짜만 리스트로 반환합니다.",
            security = @SecurityRequirement(name = "JWT")
    )
    @GetMapping(value = "/scheduled/calendar", produces = "application/json")
    public ApiResponse<List<String>> getTeumDatesOfMonth(
            @Parameter(description = "조회할 연월 (YYYY-MM)", example = "2025-05")
            @RequestParam("month") String month,
            @CurrentUser @Parameter(hidden = true) User user
    ) {
        List<String> dates = teumService.getScheduledTeumsOfMonth(user.getId(), month);
        return ApiResponse.of(TeumSuccessStatus._SCHEDULED_CALENDAR_LOADED, dates);
    }

    @Operation(
            summary = "특정 날짜의 약속된 틈 조회",
            description = "사용자가 참여 중인 틈 중, 지정한 날짜에 해당하는 틈 목록을 조회합니다."
    )
    @GetMapping(value = "/scheduled", produces = "application/json")
    public ApiResponse<List<ScheduledTeumResponseDto>> getScheduledTeums(
            @Parameter(description = "조회할 날짜 (YYYY-MM-DD)", example = "2025-05-02")
            @RequestParam("date") String date,
            @CurrentUser @Parameter(hidden = true) User user
    ) {
        List<ScheduledTeumResponseDto> result = teumService.getScheduledTeums(user.getId(), date);
        return ApiResponse.of(TeumSuccessStatus._SCHEDULED_LIST_LOADED, result);
    }

    @Operation(
            summary = "약속된 틈 상세 조회",
            description = "사용자가 참여 중인 약속된 틈(scheduleId)에 대한 상세 정보를 조회합니다."
    )
    @GetMapping(value = "/scheduled/{scheduleId}", produces = "application/json")
    public ApiResponse<ScheduledTeumDetailResponseDto> getScheduledTeumDetail(
            @Parameter(name = "scheduleId", description = "사용자 본인의 약속된 틈 일정 ID", example = "300")
            @PathVariable("scheduleId") Long scheduleId,
            @CurrentUser @Parameter(hidden = true) User user
    ) {
        return ApiResponse.of(
                TeumSuccessStatus._SCHEDULED_DETAIL_LOADED,
                teumService.getScheduledTeumDetail(scheduleId, user.getId())
        );
    }

    @Operation(
            summary = "약속된 틈 취소",
            description = "본인의 약속된 틈을 취소합니다. 마지막 1인이 남을 경우 자동으로 같이 취소됩니다."
    )
    @PatchMapping("/scheduled/{scheduleId}/cancel")
    public ApiResponse<ScheduledTeumCancelResponseDto> cancelScheduledTeum(
            @PathVariable("scheduleId") Long scheduleId,
            @CurrentUser @Parameter(hidden = true) User user
    ) {
        ScheduledTeumCancelResponseDto result = teumService.cancelScheduledTeum(scheduleId, user.getId());
        return ApiResponse.of(TeumSuccessStatus._SCHEDULED_CANCELLED, result);
    }


    @Operation(
            summary = "공통 가능한 시간대 조회",
            description = "현재 로그인 사용자와 지정된 사용자들 간의 특정 날짜에 대해 공통 가능한 시간대를 반환합니다."
    )
    @PostMapping(value = "/available-time", consumes = "application/json", produces = "application/json")
    public ApiResponse<AvailableTimeResponseDto> getAvailableTime(
            @Parameter(hidden = true) @CurrentUser User user,
            @RequestBody AvailableTimeRequestDto requestDto
    ) {
        return ApiResponse.of(TeumSuccessStatus._AVAILABLE_TIME_LOADED,
                teumService.getAvailableTime(user, requestDto));
    }

    @Operation(
            summary = "함께한 틈 시간 조회",
            description = "로그인한 사용자와 지정된 친구가 함께 참여한 틈의 횟수와 누적 시간을 분 단위로 반환합니다."
    )
    @GetMapping(value = "/{userId}/shared/teum-time", produces = "application/json")
    public ApiResponse<SharedTeumTimeResponseDto> getSharedTeumStats(
            @Parameter(hidden = true) @CurrentUser User loginUser,
            @Parameter(name = "userId", description = "조회할 친구 ID", example = "2")
            @PathVariable("userId") Long targetUserId
    ) {
        SharedTeumTimeResponseDto result = teumService.getSharedTeumStats(loginUser.getId(), targetUserId);
        return ApiResponse.of(TeumSuccessStatus._SHARED_TIME_LOADED, result);
    }

    @Operation(
            summary = "함께한 틈 목록 조회",
            description = "로그인한 사용자와 지정된 친구가 함께 참여한 모든 틈 요청 목록을 반환합니다."
    )
    @GetMapping(value = "/{userId}/shared", produces = "application/json")
    public ApiResponse<List<SharedTeumListResponseDto>> getSharedTeums(
            @Parameter(name = "userId", description = "함께한 틈을 조회할 친구 ID", example = "1")
            @PathVariable("userId") Long userId
    ) {
        return ApiResponse.onSuccess(null);
    }

}
