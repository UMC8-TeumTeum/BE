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
    public List<FollowingUserResponseDto> getFollowingsByUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new GlobalHandler(FriendErrorStatus.USER_NOT_FOUND));

        List<Friend> followings = friendRepository.findByFollowerId(user.getId());

        return FriendConverter.toFollowingUserResponseList(followings).stream()
                .sorted(Comparator
                        .comparing(FollowingUserResponseDto::getIsFavorite).reversed()
                        .thenComparing(FollowingUserResponseDto::getNickname))
                .collect(Collectors.toList());
    }

    @Override
    public List<FollowerUserResponseDto> getFollowers() {
        // TODO : 팔로워 목록 조회 로직 추후 구현
        return List.of();
    }
}
