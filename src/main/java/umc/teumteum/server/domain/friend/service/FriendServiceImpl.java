package umc.teumteum.server.domain.friend.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import umc.teumteum.server.domain.friend.controller.FriendController;
import umc.teumteum.server.domain.friend.converter.FriendConverter;
import umc.teumteum.server.domain.friend.dto.*;
import umc.teumteum.server.domain.friend.entity.Friend;
import umc.teumteum.server.domain.friend.exception.status.FriendErrorStatus;
import umc.teumteum.server.domain.friend.repository.FriendRepository;
import umc.teumteum.server.domain.home.entity.Schedule;
import umc.teumteum.server.domain.home.repository.ScheduleRepository;
import umc.teumteum.server.domain.user.entity.User;
import umc.teumteum.server.domain.user.repository.UserRepository;
import umc.teumteum.server.global.exception.handler.GlobalHandler;

import java.time.Duration;
import java.util.Comparator;
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

    @Override
    public List<FollowingUserResponseDto> getFollowingsByUser(Long userId, int page, int size) {
        User user = getUserOrThrow(userId);

        List<Friend> followings = friendRepository.findByFollowerId(user.getId());

        List<FollowingUserResponseDto> sortedList = friendConverter.toFollowingUserResponseList(followings).stream()
                .sorted(Comparator
                        .comparing(FollowingUserResponseDto::getIsFavorite).reversed()
                        .thenComparing(FollowingUserResponseDto::getNickname))
                .collect(Collectors.toList());

        int start = page * size;
        int end = Math.min(start + size, sortedList.size());

        return (start >= sortedList.size()) ? List.of() : sortedList.subList(start, end);
    }

    @Override
    public List<FollowerUserResponseDto> getFollowersByUser(Long userId, int page, int size) {
        User user = getUserOrThrow(userId);

        List<Friend> followers = friendRepository.findByFollowingId(user.getId());

        List<FollowerUserResponseDto> sortedList = friendConverter.toFollowerUserResponseList(followers).stream()
                .sorted(Comparator.comparing(FollowerUserResponseDto::getNickname))
                .collect(Collectors.toList());

        int start = page * size;
        int end = Math.min(start + size, sortedList.size());

        return (start >= sortedList.size()) ? List.of() : sortedList.subList(start, end);
    }

    @Override
    public FriendProfileResponseDto getFriendProfile(Long loginUserId, Long targetUserId) {
        if (loginUserId.equals(targetUserId)) {
            throw new GlobalHandler(FriendErrorStatus.CANNOT_VIEW_SELF);
        }

        User loginUser = getUserOrThrow(loginUserId);
        User targetUser = getUserOrThrow(targetUserId);

        Optional<Friend> followRelationOpt = friendRepository
                .findByFollowerIdAndFollowingId(loginUser.getId(), targetUser.getId());

        return friendConverter.toFriendProfileResponse(targetUser, followRelationOpt.orElse(null));
    }

    @Override
    public Long getFriendTeumTime(Long userId) {
        User user = getUserOrThrow(userId);
        List<Schedule> schedules = scheduleRepository.findByUserIdAndIncludeTeumIsTrue(userId);
        return friendConverter.calculateTeumTime(schedules);
    }

    private User getUserOrThrow(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new GlobalHandler(FriendErrorStatus.USER_NOT_FOUND));
    }

}
