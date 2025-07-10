package umc.teumteum.server.domain.teum.service;

import umc.teumteum.server.domain.teum.dto.TeumRequestDto;
import umc.teumteum.server.domain.teum.dto.TeumResendRequestDto;

public interface TeumService {

    Long createRequest(TeumRequestDto requestDto);

    Long createResendRequest(Long parentRequestId, TeumResendRequestDto resendRequestDto);

}