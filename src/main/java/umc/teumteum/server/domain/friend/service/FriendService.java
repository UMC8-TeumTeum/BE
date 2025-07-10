package umc.teumteum.server.domain.friend.service;

import umc.teumteum.server.domain.friend.dto.FriendMutualResponseDto;

import java.util.List;

public interface FriendService {
    Long follow(Long userId);
    void unfollow(Long userId);

    List<FriendMutualResponseDto> getMutualFriends();
}
