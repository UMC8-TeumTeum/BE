package umc.teumteum.server.domain.friend.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import umc.teumteum.server.domain.friend.controller.FriendController;
import umc.teumteum.server.domain.friend.converter.FriendConverter;
import umc.teumteum.server.domain.friend.dto.FavoriteResponseDto;
import umc.teumteum.server.domain.friend.dto.FollowerUserResponseDto;
import umc.teumteum.server.domain.friend.dto.FollowingUserResponseDto;
import umc.teumteum.server.domain.friend.dto.FriendMutualResponseDto;
import umc.teumteum.server.domain.friend.entity.Friend;
import umc.teumteum.server.domain.friend.exception.status.FriendErrorStatus;
import umc.teumteum.server.domain.friend.repository.FriendRepository;
import umc.teumteum.server.domain.user.entity.User;
import umc.teumteum.server.domain.user.repository.UserRepository;
import umc.teumteum.server.global.exception.handler.GlobalHandler;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FriendServiceImpl implements FriendService {

    private final UserRepository userRepository;
    private final FriendRepository friendRepository;

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
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new GlobalHandler(FriendErrorStatus.USER_NOT_FOUND));

        List<Friend> followings = friendRepository.findByFollowerId(user.getId());

        List<FollowingUserResponseDto> sortedList = FriendConverter.toFollowingUserResponseList(followings).stream()
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
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new GlobalHandler(FriendErrorStatus.USER_NOT_FOUND));

        List<Friend> followers = friendRepository.findByFollowingId(user.getId());

        List<FollowerUserResponseDto> sortedList = FriendConverter.toFollowerUserResponseList(followers).stream()
                .sorted(Comparator.comparing(FollowerUserResponseDto::getNickname))
                .collect(Collectors.toList());

        int start = page * size;
        int end = Math.min(start + size, sortedList.size());

        return (start >= sortedList.size()) ? List.of() : sortedList.subList(start, end);
    }

}
