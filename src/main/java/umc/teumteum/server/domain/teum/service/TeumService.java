package umc.teumteum.server.domain.teum.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import umc.teumteum.server.domain.teum.dto.availability.AvailableTimeRequestDto;
import umc.teumteum.server.domain.teum.dto.availability.AvailableTimeResponseDto;
import umc.teumteum.server.domain.teum.dto.schedule.ScheduledTeumDetailResponseDto;
import umc.teumteum.server.domain.teum.dto.schedule.ScheduledTeumExitResponseDto;
import umc.teumteum.server.domain.teum.dto.schedule.ScheduledTeumResponseDto;
import umc.teumteum.server.domain.teum.dto.shared.SharedTeumResponseDto;
import umc.teumteum.server.domain.teum.dto.teum.*;
import umc.teumteum.server.domain.user.entity.User;

import java.util.List;

public interface TeumService {

    Long createRequest(TeumRequestDto requestDto, User user);

    Long createResendRequest(Long parentRequestId, TeumResendRequestDto resendRequestDto, User user);

    Page<TeumReceivedResponseDto> getReceivedRequests(Long userId, Pageable pageable);

    Long updateReadStatus(Long responseId, Long userId);

    TeumStatusUpdateResponseDto updateResponseStatus(Long responseId, Long userId, TeumStatusUpdateRequestDto requestDto);

    List<String> getScheduledTeumsOfMonth(Long userId, String month);

    List<ScheduledTeumResponseDto> getScheduledTeums(Long userId, String date);

    ScheduledTeumDetailResponseDto getScheduledTeumDetail(Long teumId, Long userId);

    ScheduledTeumExitResponseDto exitScheduledTeum(Long teumId, Long userId);

    AvailableTimeResponseDto getAvailableTime(User user, AvailableTimeRequestDto requestDto);

    SharedTeumResponseDto getSharedTeumStats(Long userId, Long friendId);
}