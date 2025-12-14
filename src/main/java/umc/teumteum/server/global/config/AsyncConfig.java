package umc.teumteum.server.global.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

@Configuration
public class AsyncConfig implements AsyncConfigurer {

    @Override
    public Executor getAsyncExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(5);       // 최소 스레드 수
        executor.setMaxPoolSize(10);       // 최대 스레드 수
        executor.setQueueCapacity(30);     // 대기열 크기
        executor.setThreadNamePrefix("EVENT-");
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());    // 거부 정책: 스레드 풀이 꽉 차면 호출 스레드에서 동기 실행
        executor.initialize();
        return executor;
    }
}
