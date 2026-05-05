package umc.teumteum.server.global.ratelimit;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
public class RateLimiter {

    @Resource(name = "rateLimitRedisTemplate")
    private RedisTemplate<String, String> rateLimitRedisTemplate;

    private static final String SLIDING_WINDOW_COUNTER_SCRIPT = """
            local current_key = KEYS[1]
            local previous_key = KEYS[2]
            local now = tonumber(ARGV[1])
            local window_ms = tonumber(ARGV[2])
            local limit = tonumber(ARGV[3])
            
            local window_start = math.floor(now / window_ms) * window_ms
            local elapsed_ratio = (now - window_start) / window_ms
            
            local current_count = tonumber(redis.call('GET', current_key) or 0)
            local previous_count = tonumber(redis.call('GET', previous_key) or 0)
            
            -- 이전 윈도우 가중 합산 (경과 비율만큼 비중 감소)
            local weighted = math.floor(previous_count * (1 - elapsed_ratio)) + current_count
            
            if weighted < limit then
                redis.call('INCR', current_key)
                redis.call('PEXPIRE', current_key, window_ms * 2)
                return weighted + 1
            else
                return -1
            end
            """;

    public boolean isAllowed(String key, RateLimitPolicy policy) {
        long windowMs = policy.getWindowMs();
        long now = System.currentTimeMillis();

        // 1. 현재/이전 윈도우 키 생성
        long currentWindow = now / windowMs;
        String currentKey = key + ":" + currentWindow;
        String previousKey = key + ":" + (currentWindow - 1);

        // 2. Lua Script 실행
        try {
            Long result = rateLimitRedisTemplate.execute(
                    new DefaultRedisScript<>(SLIDING_WINDOW_COUNTER_SCRIPT, Long.class),
                    List.of(currentKey, previousKey),
                    String.valueOf(now),
                    String.valueOf(windowMs),
                    String.valueOf(policy.getLimit())
            );
            return result != null && result != -1L;
        } catch (Exception ex) {
            // Redis 장애 시 fail-open (가용성 우선)
            log.error("[Rate Limit Check Failed]", ex);
            return true;
        }
    }
}