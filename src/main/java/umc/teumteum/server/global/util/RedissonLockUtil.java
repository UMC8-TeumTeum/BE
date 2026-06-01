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

    private static final TimeUnit DEFAULT_TIME_UNIT = TimeUnit.SECONDS;

    // 락 획득 실패 시 대기하지 않고 즉시 예외 처리
    public void executeWithLockNoWait(String key, Runnable action) {
        RLock lock = acquireLockNoWait(key);
        try {
            action.run();
        } finally {
            releaseLock(lock);
        }
    }

    // key로 락을 즉시 획득
    public RLock acquireLockNoWait(String key) {
        RLock lock = redissonClient.getLock(key);
        try {
            boolean isLocked = lock.tryLock(0, DEFAULT_TIME_UNIT);
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
