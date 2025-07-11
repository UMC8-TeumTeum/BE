package umc.teumteum.server.domain.teum.service;

import umc.teumteum.server.domain.teum.dto.*;

import java.util.List;

public interface TeumService {

    Long createRequest(TeumRequestDto requestDto);

    Long createResendRequest(Long parentRequestId, TeumResendRequestDto resendRequestDto);

    List<TeumReceivedResponseDto> getReceivedRequests(Long userId);

    TeumRequestDetailResponseDto getRequestDetail(Long responseId, Long userId);

    TeumStatusUpdateResponseDto updateResponseStatus(Long responseId, Long userId, TeumStatusUpdateRequestDto requestDto);

    List<ScheduledTeumResponseDto> getScheduledTeums(Long userId, int year, int month);

    ScheduledTeumDetailResponseDto getScheduledTeumDetail(Long teumId, Long userId);

    ScheduledTeumExitResponseDto exitScheduledTeum(Long teumId, Long userId);

    AvailableTimeResponseDto getAvailableTime(AvailableTimeRequestDto requestDto);

}