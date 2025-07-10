package umc.teumteum.server.domain.teum.service;

import umc.teumteum.server.domain.teum.dto.TeumRequestDto;

public interface TeumService {

    Long createRequest(TeumRequestDto requestDto);
}