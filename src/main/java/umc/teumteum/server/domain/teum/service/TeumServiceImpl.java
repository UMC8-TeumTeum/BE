package umc.teumteum.server.domain.teum.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import umc.teumteum.server.domain.teum.dto.TeumReceivedResponseDto;
import umc.teumteum.server.domain.teum.dto.TeumRequestDto;
import umc.teumteum.server.domain.teum.dto.TeumResendRequestDto;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TeumServiceImpl implements TeumService {

    @Override
    @Transactional
    public Long createRequest(TeumRequestDto requestDto) {
        // TODO : 틈 요청 로직 추후 구현
        return 1L;
    }

    @Override
    @Transactional
    public Long createResendRequest(Long parentRequestId, TeumResendRequestDto resendRequestDto) {
        // TODO: 틈 요청 로직 추후 구현
        return 1L;
    }

    @Override
    public List<TeumReceivedResponseDto> getReceivedRequests(Long userId) {
        // TODO: 틈 요청 불러오기 로직 추후 구현
        return List.of();
    }
}
