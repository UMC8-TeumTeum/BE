package umc.teumteum.server.domain.teum.service;

import org.springframework.data.domain.Page;
import umc.teumteum.server.domain.teum.dto.TeumRequestDto;
import umc.teumteum.server.domain.teum.dto.TeumResponseDto;
import umc.teumteum.server.domain.user.entity.User;
import umc.teumteum.server.global.dto.PagingResponseDto;

import java.util.List;

public interface TeumService {

    Long createRequest(TeumRequestDto.TeumRequest requestDto, User user);

    Long createResendRequest(Long parentRequestId, TeumRequestDto.TeumResend resendRequestDto, User user);

    Page<TeumResponseDto.TeumReceived> getReceivedRequests(Long userId, int page, int size);

    Long updateReadStatus(Long responseId, Long userId);

    TeumResponseDto.TeumStatusUpdate updateResponseStatus(Long responseId, Long userId, TeumRequestDto.TeumStatusUpdate requestDto);

    List<String> getTeumRequestsOfMonth(Long userId, String month);

    List<String> getScheduledTeumsOfMonth(Long userId, String month);

    List<TeumResponseDto.ScheduledTeum> getScheduledTeums(Long userId, String date);

    TeumResponseDto.ScheduledTeumDetail getScheduledTeumDetail(Long scheduleId, Long userId);

    TeumResponseDto.ScheduledTeumCancel cancelScheduledTeum(Long scheduleId, Long userId);

    TeumResponseDto.TeumAvailableTime getAvailableTime(User user, TeumRequestDto.TeumAvailableTime requestDto);

    TeumResponseDto.SharedTeumTime getSharedTeumStats(Long userId, Long friendId);

    PagingResponseDto<TeumResponseDto.SharedTeumList> getSharedTeums(Long loginUserId, Long targetUserId, int page, int size);

    List<TeumResponseDto.TeumRequestDetail> getTeumRequestsByDate(Long userId, String date);

    Long cancelTeumRequest(Long requestId, Long userId);

    TeumResponseDto.ConflictingScheduleResponse checkConflictingSchedules(Long userId, TeumRequestDto.ConflictCheckRequest request);

}