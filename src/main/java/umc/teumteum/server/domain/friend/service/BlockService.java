package umc.teumteum.server.domain.friend.service;

import umc.teumteum.server.domain.user.entity.User;

public interface BlockService {
    void blockUser(User loginUser, Long targetUserId);
}