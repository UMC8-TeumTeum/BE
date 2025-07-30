package umc.teumteum.server.domain.friend.service;

import umc.teumteum.server.domain.friend.dto.*;
import umc.teumteum.server.global.dto.PagingResponseDto;

import java.util.List;

public interface FriendService {
    Long follow(Long userId);

    void unfollow(Long userId);

    List<FriendMutualResponseDto> getMutualFriends();

    FavoriteResponseDto updateFavorite(Long userId, Boolean isFavorite);

    PagingResponseDto<FollowingUserResponseDto> getFollowingsByUser(Long userId, int page, int size);

    PagingResponseDto<FollowerUserResponseDto> getFollowersByUser(Long userId, int page, int size);

    FriendProfileResponseDto getFriendProfile(Long loginUserId, Long targetUserId);

    FriendTeumTimeResponseDto getFriendTeumTime(Long loginUserId, Long targetUserId);

}
