package umc.teumteum.server.domain.friend.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import umc.teumteum.server.domain.user.entity.User;
import umc.teumteum.server.global.util.RedissonLockUtil;

@Service
@RequiredArgsConstructor
public class FriendLockServiceImpl implements FriendLockService {

    private final RedissonLockUtil redissonLockUtil;
    private final FriendService friendService;

    // 팔로우 요청 시 Redisson 락을 걸고 follow 로직 실행
    @Override
    public void followWithLock(User loginUser, Long targetUserId) {
        String lockKey = createLockKey(loginUser.getId(), targetUserId);
        redissonLockUtil.executeWithLock(lockKey,
                () -> friendService.follow(loginUser, targetUserId)
        );
    }

    private String createLockKey(Long loginUserId, Long targetUserId) {
        return String.format("FOLLOW_LOCK:%d:%d", loginUserId, targetUserId);
    }
}
