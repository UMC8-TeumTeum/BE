package umc.teumteum.server.domain.teum.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import umc.teumteum.server.domain.teum.dto.availability.AvailableTimeRequestDto;
import umc.teumteum.server.domain.teum.dto.availability.AvailableTimeResponseDto;
import umc.teumteum.server.domain.teum.dto.schedule.ScheduledTeumDetailResponseDto;
import umc.teumteum.server.domain.teum.dto.schedule.ScheduledTeumCancelResponseDto;
import umc.teumteum.server.domain.teum.dto.schedule.ScheduledTeumResponseDto;
import umc.teumteum.server.domain.teum.dto.shared.SharedTeumTimeResponseDto;
import umc.teumteum.server.domain.teum.dto.teum.*;
import umc.teumteum.server.domain.user.entity.User;

import java.util.List;

public interface TeumService {

    Long createRequest(TeumRequestDto requestDto, User user);

    Long createResendRequest(Long parentRequestId, TeumResendRequestDto resendRequestDto, User user);

    Page<TeumReceivedResponseDto> getReceivedRequests(Long userId, int page, int size);

    Long updateReadStatus(Long responseId, Long userId);

    TeumStatusUpdateResponseDto updateResponseStatus(Long responseId, Long userId, TeumStatusUpdateRequestDto requestDto);

    List<String> getTeumRequestsOfMonth(Long userId, String month);

    List<String> getScheduledTeumsOfMonth(Long userId, String month);

    List<ScheduledTeumResponseDto> getScheduledTeums(Long userId, String date);

    ScheduledTeumDetailResponseDto getScheduledTeumDetail(Long scheduleId, Long userId);

    ScheduledTeumCancelResponseDto cancelScheduledTeum(Long scheduleId, Long userId);

    AvailableTimeResponseDto getAvailableTime(User user, AvailableTimeRequestDto requestDto);

    SharedTeumTimeResponseDto getSharedTeumStats(Long userId, Long friendId);

    List<TeumRequestResponseDto> getTeumRequestsByDate(Long userId, String date);
}