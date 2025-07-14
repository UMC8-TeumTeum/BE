package umc.teumteum.server.domain.teum.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import umc.teumteum.server.domain.teum.dto.availability.AvailableTimeRequestDto;
import umc.teumteum.server.domain.teum.dto.availability.AvailableTimeResponseDto;
import umc.teumteum.server.domain.teum.dto.schedule.ScheduledTeumDetailResponseDto;
import umc.teumteum.server.domain.teum.dto.schedule.ScheduledTeumExitResponseDto;
import umc.teumteum.server.domain.teum.dto.schedule.ScheduledTeumResponseDto;
import umc.teumteum.server.domain.teum.dto.shared.SharedTeumResponseDto;
import umc.teumteum.server.domain.teum.dto.teum.*;
import umc.teumteum.server.domain.teum.entity.TeumRequest;
import umc.teumteum.server.domain.teum.entity.TeumResponse;
import umc.teumteum.server.domain.teum.entity.enums.ResponseStatus;
import umc.teumteum.server.domain.teum.exception.status.TeumErrorStatus;
import umc.teumteum.server.domain.teum.repository.TeumRequestRepository;
import umc.teumteum.server.domain.teum.repository.TeumResponseRepository;
import umc.teumteum.server.domain.user.entity.User;
import umc.teumteum.server.domain.user.repository.UserRepository;
import umc.teumteum.server.global.exception.GeneralException;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TeumServiceImpl implements TeumService {

    private final UserRepository userRepository;
    private final TeumRequestRepository teumRequestRepository;
    private final TeumResponseRepository teumResponseRepository;

    @Override
    @Transactional
    public Long createRequest(TeumRequestDto dto) {
        // senderUserId로 요청자 조회 (임시방안)
        User sender = userRepository.findById(dto.getSenderUserId())
                .orElseThrow(() -> new GeneralException(TeumErrorStatus.USER_NOT_ELIGIBLE));

        TeumRequest parent = null;
        if (dto.getParentRequestId() != null) {
            parent = teumRequestRepository.findById(dto.getParentRequestId())
                    .orElseThrow(() -> new GeneralException(TeumErrorStatus.INVALID_PARENT_REQUEST));
        }

        TeumRequest request = TeumRequest.builder()
                .title(dto.getTitle())
                .description(dto.getDescription())
                .date(LocalDate.parse(dto.getDate()))
                .startTime(LocalTime.parse(dto.getStartTime()))
                .endTime(LocalTime.parse(dto.getEndTime()))
                .graphicId(dto.getGraphicId())
                .user(sender)
                .parentRequest(parent)
                .build();

        List<TeumResponse> responses = dto.getReceiverUserIds().stream()
                .distinct()
                .map(receiverId -> {
                    if (receiverId.equals(sender.getId())) {
                        throw new GeneralException(TeumErrorStatus.CANNOT_REQUEST_SELF);
                    }
                    User receiver = userRepository.findById(receiverId)
                            .orElseThrow(() -> new GeneralException(TeumErrorStatus.USER_NOT_ELIGIBLE));
                    return TeumResponse.builder()
                            .teumRequest(request)
                            .receiverUser(receiver)
                            .status(ResponseStatus.PENDING)
                            .readAt(null)
                            .message("")
                            .build();
                }).toList();

        request.getTeumResponses().addAll(responses);
        teumRequestRepository.save(request);

        return request.getId();
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
    public List<String> getScheduledTeumsOfMonth(Long userId, String month) {
        // TODO: 약속된 틈의 날짜 리스트 조회 로직 추후 구현
        return List.of();
    }

    @Override
    public List<ScheduledTeumResponseDto> getScheduledTeums(Long userId, String date) {
        // TODO: 특정 날짜의 약속된 틈 조회 로직 추후 구현
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
