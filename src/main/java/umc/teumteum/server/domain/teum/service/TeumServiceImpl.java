package umc.teumteum.server.domain.teum.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import umc.teumteum.server.domain.teum.converter.TeumConverter;
import umc.teumteum.server.domain.teum.dto.availability.AvailableTimeRequestDto;
import umc.teumteum.server.domain.teum.dto.availability.AvailableTimeResponseDto;
import umc.teumteum.server.domain.teum.dto.schedule.ScheduledTeumDetailResponseDto;
import umc.teumteum.server.domain.teum.dto.schedule.ScheduledTeumExitResponseDto;
import umc.teumteum.server.domain.teum.dto.schedule.ScheduledTeumResponseDto;
import umc.teumteum.server.domain.teum.dto.shared.SharedTeumResponseDto;
import umc.teumteum.server.domain.teum.dto.teum.*;
import umc.teumteum.server.domain.teum.entity.TeumRequest;
import umc.teumteum.server.domain.teum.entity.TeumResponse;
import umc.teumteum.server.domain.teum.entity.enums.RequestStatus;
import umc.teumteum.server.domain.teum.entity.enums.ResponseStatus;
import umc.teumteum.server.domain.teum.exception.status.TeumErrorStatus;
import umc.teumteum.server.domain.teum.repository.TeumRequestRepository;
import umc.teumteum.server.domain.teum.repository.TeumResponseRepository;
import umc.teumteum.server.domain.user.entity.User;
import umc.teumteum.server.domain.user.repository.UserRepository;
import umc.teumteum.server.global.exception.GeneralException;
import umc.teumteum.server.global.util.S3Util;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TeumServiceImpl implements TeumService {

    private final S3Util s3Util;
    private final UserRepository userRepository;
    private final TeumRequestRepository teumRequestRepository;
    private final TeumResponseRepository teumResponseRepository;

    @Override
    @Transactional
    public Long createRequest(TeumRequestDto dto) {
        User sender = getUserOrThrow(dto.getSenderUserId());

        TeumRequest request = TeumConverter.toTeumRequest(dto, sender);

        List<TeumResponse> responses = TeumConverter.toTeumResponses(
                dto.getReceiverUserIds(),
                sender.getId(),
                request,
                this::getUserOrThrow
        );

        request.getTeumResponses().addAll(responses);
        teumRequestRepository.save(request);

        return request.getId();
    }


    private User getUserOrThrow(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new GeneralException(TeumErrorStatus.USER_NOT_ELIGIBLE));
    }

    @Override
    @Transactional
    public Long createResendRequest(Long parentRequestId, TeumResendRequestDto resendRequestDto) {
        // TODO: 틈 요청 로직 추후 구현
        return 1L;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<TeumReceivedResponseDto> getReceivedRequests(Long userId, Pageable pageable) {
        getUserOrThrow(userId);

        LocalDate today = LocalDate.now();
        LocalTime now = LocalTime.now();

        Page<TeumResponse> page = teumResponseRepository.findValidPendingResponses(userId, today, now, pageable);

        List<TeumReceivedResponseDto> dtoList = page.getContent().stream()
                .map(response -> TeumConverter.toReceivedResponseDto(response, s3Util))
                .toList();

        return new PageImpl<>(dtoList, pageable, page.getTotalElements());
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
