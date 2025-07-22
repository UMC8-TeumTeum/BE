package umc.teumteum.server.domain.friend.service;

import umc.teumteum.server.domain.friend.dto.*;

import java.util.List;

public interface FriendService {
    Long follow(Long userId);

    void unfollow(Long userId);

    List<FriendMutualResponseDto> getMutualFriends();

    FavoriteResponseDto updateFavorite(Long userId, Boolean isFavorite);

    List<FollowingUserResponseDto> getFollowingsByUser(Long userId, int page, int size);

    List<FollowerUserResponseDto> getFollowersByUser(Long userId, int page, int size);

    FriendProfileResponseDto getFriendProfile(Long loginUserId, Long targetUserId);

    Long getFriendTeumTime(Long loginUserId, Long targetUserId);

}
