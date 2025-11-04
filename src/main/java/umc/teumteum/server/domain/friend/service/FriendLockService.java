package umc.teumteum.server.domain.friend.service;

import umc.teumteum.server.domain.user.entity.User;

public interface FriendLockService {
    void followWithLock(User loginUser, Long targetUserId);
}
