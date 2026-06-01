package umc.teumteum.server.integration.user.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.test.context.ActiveProfiles;
import umc.teumteum.server.domain.home.repository.ScheduleRepository;
import umc.teumteum.server.domain.user.dto.OnboardingRequestDto;
import umc.teumteum.server.domain.user.entity.User;
import umc.teumteum.server.domain.user.entity.enums.SocialType;
import umc.teumteum.server.domain.user.entity.enums.UserStep;
import umc.teumteum.server.domain.user.entity.enums.Weekday;
import umc.teumteum.server.domain.user.repository.RoutineRepository;
import umc.teumteum.server.domain.user.repository.UserRepository;
import umc.teumteum.server.domain.user.service.OnboardingLockService;
import umc.teumteum.server.domain.user.service.OnboardingService;
import umc.teumteum.server.global.util.S3Util;
import umc.teumteum.server.support.RedisTestContainerSupport;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;

@SpringBootTest
@ActiveProfiles("test")
@DisplayName("OnboardingLockService 관련 통합 테스트")
public class OnboardingLockServiceTest extends RedisTestContainerSupport {

    @Autowired
    private OnboardingLockService onboardingLockService;

    @Autowired
    private RedissonClient redissonClient;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoutineRepository routineRepository;

    @Autowired
    private ScheduleRepository scheduleRepository;

    @MockBean
    private S3Util s3Util;

    @SpyBean
    private OnboardingService onboardingService;

    private User user;

    @BeforeEach
    void setUp() {
        scheduleRepository.deleteAll();
        routineRepository.deleteAll();
        userRepository.deleteAll();
        redissonClient.getKeys().flushall();

        user = userRepository.save(
                User.builder()
                        .email("teumteum@kakao.com")
                        .socialId(UUID.randomUUID().toString())
                        .socialType(SocialType.KAKAO)
                        .step(UserStep.ONBOARDING)
                        .build()
        );
    }

    @Test
    @DisplayName("saveRoutines 여러 번 요청 시 한 번만 성공 - Redisson 분산 락으로 동시성 제어")
    void saveRoutines_ConcurrencyControl() throws InterruptedException {
        // given
        int threadCount = 10;
        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);

        CountDownLatch readyLatch = new CountDownLatch(threadCount);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(threadCount);

        AtomicInteger successCount = new AtomicInteger();
        AtomicInteger failCount = new AtomicInteger();

        OnboardingRequestDto.RoutineListRequest request = createRoutines(
                createRoutineDTO(todayWeekday(), LocalTime.of(9, 0), LocalTime.of(10, 0), "아침 루틴"),
                createRoutineDTO(todayWeekday(), LocalTime.of(11, 0), LocalTime.of(12, 0), "점심 루틴")
        );
        doAnswer(invocation -> {
            Thread.sleep(300);
            return invocation.callRealMethod();
        }).when(onboardingService).saveRoutines(any(OnboardingRequestDto.RoutineListRequest.class), any(User.class));

        // when
        for (int i = 0; i < threadCount; i++) {
            executorService.submit(() -> {
                try {
                    readyLatch.countDown();
                    startLatch.await();

                    onboardingLockService.saveRoutinesWithLock(request, user);
                    successCount.incrementAndGet();
                } catch (Exception e) {
                    failCount.incrementAndGet();
                } finally {
                    doneLatch.countDown();
                }
            });
        }

        try {
            assertThat(readyLatch.await(5, TimeUnit.SECONDS))
                    .as("모든 테스트 스레드가 준비 상태에 도달해야 합니다.")
                    .isTrue();

            startLatch.countDown();

            assertThat(doneLatch.await(10, TimeUnit.SECONDS))
                    .as("모든 테스트 스레드가 제한 시간 내 종료되어야 합니다.")
                    .isTrue();
        } finally {
            executorService.shutdownNow();
        }

        // then
        assertThat(successCount.get()).isEqualTo(1);
        assertThat(failCount.get()).isEqualTo(threadCount - 1);
        assertThat(routineRepository.findByUser(user)).hasSize(2);
        assertThat(scheduleRepository.findByUser(user)).hasSize(2);
    }

    private static Weekday todayWeekday() {
        return Weekday.from(LocalDate.now().getDayOfWeek());
    }

    private static OnboardingRequestDto.RoutineListRequest createRoutines(OnboardingRequestDto.RoutineDTO... routines) {
        return OnboardingRequestDto.RoutineListRequest.builder()
                .routine(List.of(routines))
                .build();
    }

    private static OnboardingRequestDto.RoutineDTO createRoutineDTO(Weekday weekday, LocalTime startTime,
                                                                    LocalTime endTime,
                                                                    String title) {
        return OnboardingRequestDto.RoutineDTO.builder()
                .weekday(weekday)
                .startTime(startTime)
                .endTime(endTime)
                .title(title)
                .build();
    }
}
