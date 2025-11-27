package umc.teumteum.server.domain.friend.service;

import umc.teumteum.server.domain.friend.dto.BlockResponseDto;
import umc.teumteum.server.domain.user.entity.User;
import umc.teumteum.server.global.dto.PagingResponseDto;

public interface BlockService {
    void blockUser(User loginUser, Long targetUserId);

    void unblockUser(User loginUser, Long targetUserId);

    PagingResponseDto<BlockResponseDto.BlockedFriend> getBlockedUsers(User loginUser, int page, int size);
}