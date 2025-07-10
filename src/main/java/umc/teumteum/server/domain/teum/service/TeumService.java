package umc.teumteum.server.domain.teum.service;

import umc.teumteum.server.domain.teum.dto.TeumReceivedResponseDto;
import umc.teumteum.server.domain.teum.dto.TeumRequestDetailResponseDto;
import umc.teumteum.server.domain.teum.dto.TeumRequestDto;
import umc.teumteum.server.domain.teum.dto.TeumResendRequestDto;

import java.util.List;

public interface TeumService {

    Long createRequest(TeumRequestDto requestDto);

    Long createResendRequest(Long parentRequestId, TeumResendRequestDto resendRequestDto);

    List<TeumReceivedResponseDto> getReceivedRequests(Long userId);

    TeumRequestDetailResponseDto getRequestDetail(Long responseId, Long userId);

}