package umc.teumteum.server.integration.friend.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import umc.teumteum.server.domain.friend.exception.FriendException;
import umc.teumteum.server.domain.friend.repository.FriendRepository;
import umc.teumteum.server.domain.friend.service.FriendLockService;
import umc.teumteum.server.domain.notification.service.NotificationUseCases;
import umc.teumteum.server.domain.user.entity.User;
import umc.teumteum.server.domain.user.entity.enums.SocialType;
import umc.teumteum.server.domain.user.entity.enums.UserStep;
import umc.teumteum.server.domain.user.repository.UserRepository;
import umc.teumteum.server.global.exception.handler.GlobalHandler;
import umc.teumteum.server.global.util.S3Util;

@SpringBootTest
@ActiveProfiles("test")
@DisplayName("FriendLockService 관련 통합 테스트")
public class FriendLockServiceTest {

    @Autowired
    private FriendLockService friendLockService;

    @Autowired
    private RedissonClient redissonClient;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private FriendRepository friendRepository;

    @MockBean
    private NotificationUseCases notificationUseCases;

    @MockBean
    private S3Util s3Util;

    private User loginUser;
    private User targetUser;

    @BeforeEach
    void setUp() {
        friendRepository.deleteAll();
        userRepository.deleteAll();
        redissonClient.getKeys().flushall();

        loginUser = userRepository.save(
                User.builder()
                        .email("teumteum@kakao.com")
                        .socialId(UUID.randomUUID().toString())
                        .socialType(SocialType.KAKAO)
                        .step(UserStep.ONBOARDING)
                        .build()
        );
        targetUser = userRepository.save(
                User.builder()
                        .email("teumteum2@kakao.com")
                        .socialId(UUID.randomUUID().toString())
                        .socialType(SocialType.KAKAO)
                        .step(UserStep.ONBOARDING)
                        .build()
        );
    }

    @Test
    @DisplayName("follow 여러 번 요청 시 한 번만 성공 - Redisson 분산 락으로 동시성 제어")
    void followFriend_ConcurrencyControl() throws InterruptedException {
        // given
        int threadCount = 10;
        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);

        AtomicInteger successCount = new AtomicInteger();
        AtomicInteger failCount = new AtomicInteger();

        // when
        for (int i = 0; i < threadCount; i++) {
            executorService.submit(() -> {
                try {
                    friendLockService.followWithLock(loginUser, targetUser.getId());
                    successCount.incrementAndGet();
                } catch (FriendException | GlobalHandler e) {
                    failCount.incrementAndGet();
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();
        executorService.shutdown();

        long totalSaved = friendRepository.count();

        // then
        assertThat(successCount.get()).isEqualTo(1);
        assertThat(failCount.get()).isEqualTo(threadCount-1);
        assertThat(totalSaved).isEqualTo(1);
    }
}
