package umc.teumteum.server.domain.teum.service;

import org.springframework.data.domain.Page;
import umc.teumteum.server.domain.teum.dto.TeumRequestDto;
import umc.teumteum.server.domain.teum.dto.TeumResponseDto;
import umc.teumteum.server.domain.teum.dto.teum.*;
import umc.teumteum.server.domain.user.entity.User;
import umc.teumteum.server.global.dto.PagingResponseDto;

import java.util.List;

public interface TeumService {

    Long createRequest(TeumRequestDto.TeumRequest requestDto, User user);

    Long createResendRequest(Long parentRequestId, TeumResendRequestDto resendRequestDto, User user);

    Page<TeumResponseDto.TeumReceived> getReceivedRequests(Long userId, int page, int size);

    Long updateReadStatus(Long responseId, Long userId);

    TeumStatusUpdateResponseDto updateResponseStatus(Long responseId, Long userId, TeumStatusUpdateRequestDto requestDto);

    List<String> getTeumRequestsOfMonth(Long userId, String month);

    List<String> getScheduledTeumsOfMonth(Long userId, String month);

    List<TeumResponseDto.ScheduledTeum> getScheduledTeums(Long userId, String date);

    TeumResponseDto.ScheduledTeumDetail getScheduledTeumDetail(Long scheduleId, Long userId);

    TeumResponseDto.ScheduledTeumCancel cancelScheduledTeum(Long scheduleId, Long userId);

    TeumResponseDto.TeumAvailableTime getAvailableTime(User user, umc.teumteum.server.domain.teum.dto.TeumRequestDto.TeumAvailableTime requestDto);

    TeumResponseDto.SharedTeumTime getSharedTeumStats(Long userId, Long friendId);

    PagingResponseDto<TeumResponseDto.SharedTeumList> getSharedTeums(Long loginUserId, Long targetUserId, int page, int size);

    List<TeumResponseDto.TeumRequestDetail> getTeumRequestsByDate(Long userId, String date);
}