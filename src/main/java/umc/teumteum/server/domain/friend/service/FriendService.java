package umc.teumteum.server.domain.friend.service;

import umc.teumteum.server.domain.friend.dto.FollowerUserResponseDto;
import umc.teumteum.server.domain.friend.dto.FollowingUserResponseDto;
import umc.teumteum.server.domain.friend.dto.FriendMutualResponseDto;
import umc.teumteum.server.domain.friend.dto.FavoriteResponseDto;

import java.util.List;

public interface FriendService {
    Long follow(Long userId);
    void unfollow(Long userId);

    List<FriendMutualResponseDto> getMutualFriends();

    FavoriteResponseDto updateFavorite(Long userId, Boolean isFavorite);

    List<FollowingUserResponseDto> getFollowings();

    List<FollowerUserResponseDto> getFollowers();
}
