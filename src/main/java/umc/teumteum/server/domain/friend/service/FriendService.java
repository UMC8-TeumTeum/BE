package umc.teumteum.server.domain.friend.service;

import umc.teumteum.server.domain.friend.dto.*;
import umc.teumteum.server.domain.user.entity.User;
import umc.teumteum.server.global.dto.PagingResponseDto;

import java.util.List;

public interface FriendService {
    void follow(User loginUser, Long targetUserId);

    void unfollow(User loginUser, Long targetUserId);

    PagingResponseDto<FriendResponseDto.MutualFriend> getMutualFriends(User loginUser, Long excludeUserId, int page, int size);

    FriendResponseDto.FriendFavorite updateFavorite(User loginUser, Long targetUserId, Boolean isFavorite);

    PagingResponseDto<FriendResponseDto.FollowingFriend> getFollowingsByUser(Long userId, int page, int size);

    PagingResponseDto<FriendResponseDto.FollowerFriend> getFollowersByUser(Long userId, int page, int size);

    FriendResponseDto.FriendProfile getFriendProfile(Long loginUserId, Long targetUserId);

    FriendTeumTimeResponseDto getFriendTeumTime(Long loginUserId, Long targetUserId);

    List<FriendPublicTodoResponseDto> getRecentPublicTodos(Long loginUserId, Long targetUserId);

    List<FriendPublicTodoResponseDto> getDailyPublicTodos(Long loginUserId, Long targetUserId, String date);

    List<String> getTodoDatesOfMonth(Long loginUserId, Long targetUserId, String month);

}
