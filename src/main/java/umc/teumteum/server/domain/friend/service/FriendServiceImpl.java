package umc.teumteum.server.domain.friend.service;

import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import umc.teumteum.server.domain.friend.converter.FriendConverter;
import umc.teumteum.server.domain.friend.dto.*;
import umc.teumteum.server.domain.friend.entity.Friend;
import umc.teumteum.server.domain.friend.exception.status.FriendErrorStatus;
import umc.teumteum.server.domain.friend.repository.FriendRepository;
import umc.teumteum.server.domain.home.entity.Schedule;
import umc.teumteum.server.domain.home.entity.enums.ScheduleStatus;
import umc.teumteum.server.domain.home.entity.enums.ScheduleType;
import umc.teumteum.server.domain.home.repository.ScheduleRepository;
import umc.teumteum.server.domain.user.entity.User;
import umc.teumteum.server.domain.user.repository.UserRepository;
import umc.teumteum.server.global.dto.PagingResponseDto;
import umc.teumteum.server.global.exception.handler.GlobalHandler;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FriendServiceImpl implements FriendService {

    private final UserRepository userRepository;
    private final FriendRepository friendRepository;
    private final ScheduleRepository scheduleRepository;
    private final FriendConverter friendConverter;

    @Override
    @Transactional
    public Long follow(Long userId) {
        // TODO : 팔로우 로직 추후 구현
        return 123L;
    }

    @Override
    @Transactional
    public void unfollow(Long userId) {
        // TODO : 언팔로우 로직 추후 구현
    }

    @Override
    @Transactional(readOnly = true)
    public List<FriendMutualResponseDto> getMutualFriends() {
        // TODO : 맞팔로우 조회 로직 추후 구현
        return null;
    }

    @Override
    @Transactional
    public FavoriteResponseDto updateFavorite(Long userId, Boolean isFavorite) {
        // TODO : 즐겨찾기 로직 추후 구현
        return new FavoriteResponseDto(userId, isFavorite);
    }

    // 사용자가 팔로우한 유저 목록을 정렬 후 페이징하여 반환
    @Override
    @Transactional(readOnly = true)
    public PagingResponseDto<FollowingUserResponseDto> getFollowingsByUser(Long userId, int page, int size) {

        Pageable pageable = PageRequest.of(
                Math.max(page - 1, 0),
                size,
                Sort.by(Sort.Order.desc("isFavorite"), Sort.Order.asc("following.nickname"))
        );

        Slice<Friend> slice = friendRepository.findByFollowerId(userId, pageable);

        List<FollowingUserResponseDto> dtoList = slice.getContent().stream()
                .map(friendConverter::toFollowingUserResponse)
                .collect(Collectors.toList());

        return new PagingResponseDto<>(dtoList, slice.hasNext());
    }

    // 사용자를 팔로우한 유저 목록을 정렬 후 페이징하여 반환
    @Override
    @Transactional(readOnly = true)
    public PagingResponseDto<FollowerUserResponseDto> getFollowersByUser(Long userId, int page, int size) {

        Pageable pageable = PageRequest.of(
                Math.max(page - 1, 0),
                size,
                Sort.by("follower.nickname").ascending()
        );

        Slice<Friend> slice = friendRepository.findByFollowingId(userId, pageable);

        List<FollowerUserResponseDto> dtoList = slice.getContent().stream()
                .map(friendConverter::toFollowerUserResponse)
                .collect(Collectors.toList());

        return new PagingResponseDto<>(dtoList, slice.hasNext());
    }

    @Override
    public FriendProfileResponseDto getFriendProfile(Long loginUserId, Long targetUserId) {
        validateNotSelf(loginUserId, targetUserId);
        User targetUser = getUserOrThrow(targetUserId);

        Optional<Friend> followRelationOpt =
                friendRepository.findByFollowerIdAndFollowingId(loginUserId, targetUser.getId());

        return friendConverter.toFriendProfileResponse(targetUser, followRelationOpt.orElse(null));
    }

    @Override
    public FriendTeumTimeResponseDto getFriendTeumTime(Long loginUserId, Long targetUserId) {
        validateNotSelf(loginUserId, targetUserId);
        validateUserExists(targetUserId);

        List<ScheduleType> targetTypes = List.of(
                ScheduleType.AI,
                ScheduleType.WISH,
                ScheduleType.TODO,
                ScheduleType.TEUM
        );

        List<Schedule> rawSchedules = scheduleRepository.findSchedulesForTeumTime(
                targetUserId,
                LocalDateTime.now(),
                targetTypes
        );

        List<Schedule> filtered = rawSchedules.stream()
                .filter(s ->
                        (s.getType() == ScheduleType.TEUM && s.getStatus() == ScheduleStatus.COMPLETED) ||
                                (s.getType() != ScheduleType.TEUM && s.getStatus() == ScheduleStatus.ACTIVE)
                )
                .collect(Collectors.toList());

        long totalMinutes = friendConverter.calculateTeumTime(filtered);
        return friendConverter.toFriendTeumTimeResponse(totalMinutes);
    }

    @Override
    @Transactional(readOnly = true)
    public List<FriendPublicTodoResponseDto> getRecentPublicTodos(Long loginUserId, Long targetUserId) {
        validateNotSelf(loginUserId, targetUserId);
        validateUserExists(targetUserId);

        List<Schedule> rawSchedules = scheduleRepository.findAllPublicByUserId(targetUserId);

        List<Schedule> filtered = rawSchedules.stream()
                .filter(s -> {
                    if (s.getType() == ScheduleType.TEUM) {
                        return s.getStatus() == ScheduleStatus.ACTIVE || s.getStatus() == ScheduleStatus.COMPLETED;
                    } else {
                        return s.getStatus() == ScheduleStatus.ACTIVE &&
                                List.of(ScheduleType.TODO, ScheduleType.WISH, ScheduleType.AI).contains(s.getType());
                    }
                })
                .limit(2)
                .collect(Collectors.toList());

        return friendConverter.toFriendPublicTodoResponse(filtered);
    }

    @Override
    public List<FriendPublicTodoResponseDto> getDailyPublicTodos(Long userId, String date) {
        // TODO : 특정 날짜의 공개 투두 조회 로직 구현
        return List.of();
    }

    @Override
    public List<String> getTodoDatesOfMonth(Long userId, String month) {
        // TODO : 공개 투두가 있는 날짜 조회 로직 구현
        return List.of();
    }


    /**
     * 주어진 ID에 해당하는 User를 조회합니다.
     * - User 객체 자체가 필요한 경우에 사용합니다.
     * - 존재하지 않으면 예외를 던집니다.
     */
    private User getUserOrThrow(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new GlobalHandler(FriendErrorStatus.USER_NOT_FOUND));
    }

    /**
     * 주어진 ID에 해당하는 User가 존재하는지만 확인합니다.
     * - 객체 자체가 필요하지 않고, 존재 여부만 확인할 때 사용합니다.
     * - 존재하지 않으면 예외를 던집니다.
     */
    private void validateUserExists(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new GlobalHandler(FriendErrorStatus.USER_NOT_FOUND);
        }
    }

    private void validateNotSelf(Long loginUserId, Long targetUserId) {
        if (loginUserId.equals(targetUserId)) {
            throw new GlobalHandler(FriendErrorStatus.CANNOT_VIEW_SELF);
        }
    }


}
