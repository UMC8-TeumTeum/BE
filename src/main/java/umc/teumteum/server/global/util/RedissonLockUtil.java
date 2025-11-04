package umc.teumteum.server.global.util;

import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Component;
import umc.teumteum.server.global.apiPayload.code.status.ErrorStatus;
import umc.teumteum.server.global.exception.handler.GlobalHandler;

@Component
@RequiredArgsConstructor
public class RedissonLockUtil {

    private final RedissonClient redissonClient;

    private static final long DEFAULT_WAIT_TIME = 3;  // 락 대기 시간
    private static final TimeUnit DEFAULT_TIME_UNIT = TimeUnit.SECONDS;  // 시간 단위 (초)

    // 락 획득&해제 및 action 실행
    public void executeWithLock(String key, Runnable action) {
        RLock lock = acquireLock(key);
        try {
            action.run();
        } finally {
            releaseLock(lock);
        }
    }

    // key로 락을 획득
    public RLock acquireLock(String key) {
        RLock lock = redissonClient.getLock(key);
        try {
            boolean isLocked = lock.tryLock(DEFAULT_WAIT_TIME, DEFAULT_TIME_UNIT);
            if (!isLocked) {
                throw new GlobalHandler(ErrorStatus.LOCK_ACQUISITION_FAILED);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new GlobalHandler(ErrorStatus._INTERNAL_SERVER_ERROR);
        }
        return lock;
    }

    // 현재 스레드가 보유한 락 해제
    public void releaseLock(RLock lock) {
        if (lock != null && lock.isHeldByCurrentThread()) {
            lock.unlock();
        }
    }
}
