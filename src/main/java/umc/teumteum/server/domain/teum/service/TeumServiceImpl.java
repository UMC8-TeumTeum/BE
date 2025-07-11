package umc.teumteum.server.domain.teum.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import umc.teumteum.server.domain.teum.dto.*;

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

    @Override
    public TeumRequestDetailResponseDto getRequestDetail(Long responseId, Long userId) {
        // TODO: 틈 요청 상세보기 로직 추후 구현
        return null;
    }

    @Override
    public TeumStatusUpdateResponseDto updateResponseStatus(Long responseId, Long userId, TeumStatusUpdateRequestDto requestDto) {
        // TODO: 틈 응답 상태 변경 로직 추후 구현
        return null;
    }

    @Override
    public List<ScheduledTeumResponseDto> getScheduledTeums(Long userId, int year, int month) {
        // TODO: 약속된 틈 조회 로직 추후 구현
        return List.of();
    }

    @Override
    public ScheduledTeumDetailResponseDto getScheduledTeumDetail(Long teumId, Long userId) {
        // TODO: 약속된 틈 상세 조회 로직 추후 구현
        return null;
    }

    @Override
    public ScheduledTeumExitResponseDto exitScheduledTeum(Long teumId, Long userId) {
        // TODO: 약속된 틈 취소 로직 추후 구현
        return null;
    }

    @Override
    public AvailableTimeResponseDto getAvailableTime(AvailableTimeRequestDto requestDto) {
        // TODO: 시간표 계산 로직 추후 구현
        return null;
    }

    @Override
    public SharedTeumResponseDto getSharedTeumStats(Long userId, Long friendId) {
        // TODO : 함께한 틈 시간 조회 로직 추후 구현
        return null;
    }

}
