package umc.teumteum.server.unit.user.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.verify;

import java.time.LocalTime;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import umc.teumteum.server.domain.home.repository.ScheduleJdbcRepository;
import umc.teumteum.server.domain.user.dto.OnboardingRequestDto;
import umc.teumteum.server.domain.user.entity.User;
import umc.teumteum.server.domain.user.entity.enums.SocialType;
import umc.teumteum.server.domain.user.entity.enums.UserStep;
import umc.teumteum.server.domain.user.entity.enums.Weekday;
import umc.teumteum.server.domain.user.exception.OnboardingException;
import umc.teumteum.server.domain.user.exception.status.UserErrorStatus;
import umc.teumteum.server.domain.user.repository.RoutineJdbcRepository;
import umc.teumteum.server.domain.user.repository.RoutineRepository;
import umc.teumteum.server.domain.user.service.OnboardingServiceImpl;
import umc.teumteum.server.global.exception.handler.GlobalHandler;
import umc.teumteum.server.global.util.TimeUtil;
import umc.teumteum.server.support.RedisTestContainerSupport;

@ExtendWith(MockitoExtension.class)
@DisplayName("OnboardingService 관련 단위 테스트")
class OnboardingServiceTest extends RedisTestContainerSupport {

    @InjectMocks
    private OnboardingServiceImpl onboardingService;

    @Mock
    private RoutineRepository routineRepository;

    @Mock
    private RoutineJdbcRepository routineJdbcRepository;

    @Mock
    private ScheduleJdbcRepository scheduleJdbcRepository;

    @Spy
    private TimeUtil timeUtil = new TimeUtil();

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id(1L)
                .step(UserStep.ONBOARDING)
                .email("teumteum@kakao.com")
                .socialId(UUID.randomUUID().toString())
                .socialType(SocialType.KAKAO)
                .build();
    }

    // ==================== 수면패턴 테스트 ====================
    @Test
    @DisplayName("수면패턴 23시간 미만 - 정상")
    void sleep_less23h_ok() {
        // given
        LocalTime sleepTime = LocalTime.of(22, 0);
        LocalTime wakeTime = LocalTime.of(20, 0);

        // when & then
        assertDoesNotThrow(() -> onboardingService.saveSleepPattern(createSleepPattern(sleepTime, wakeTime), testUser));
        assertThat(testUser.getSleepTime()).isEqualTo(sleepTime);
        assertThat(testUser.getWakeTime()).isEqualTo(wakeTime);
    }

    @Test
    @DisplayName("수면패턴 23시간 - 정상")
    void sleep_23h_ok() {
        // given
        LocalTime sleepTime = LocalTime.of(22, 0);
        LocalTime wakeTime = LocalTime.of(21, 0);

        // when & then
        assertDoesNotThrow(() -> onboardingService.saveSleepPattern(createSleepPattern(sleepTime, wakeTime), testUser));
        assertThat(testUser.getSleepTime()).isEqualTo(sleepTime);
        assertThat(testUser.getWakeTime()).isEqualTo(wakeTime);
    }

    @Test
    @DisplayName("수면패턴 23시간 초과 - 예외")
    void sleep_over23h_fail() {
        // given
        LocalTime sleepTime = LocalTime.of(22, 0);
        LocalTime wakeTime = LocalTime.of(21, 30);

        // when & then
        OnboardingException exception = assertThrows(OnboardingException.class,
                () -> onboardingService.saveSleepPattern(createSleepPattern(sleepTime, wakeTime), testUser)
        );
        assertEquals(UserErrorStatus.INVALID_SLEEP_DURATION.getCode(), exception.getErrorReason().getCode());
        assertEquals(UserErrorStatus.INVALID_SLEEP_DURATION.getMessage(), exception.getErrorReason().getMessage());
    }

    // ==================== 반복일정 테스트 ====================
    @ParameterizedTest
    @DisplayName("반복일정 정상 케이스")
    @MethodSource("getValidRoutines")
    void validRoutine(OnboardingRequestDto.RoutineListRequest routine) {
        // when & then
        assertDoesNotThrow(() -> onboardingService.saveRoutines(routine, testUser));
        verify(routineJdbcRepository).batchInsertRoutines(anyList());
    }

    @ParameterizedTest
    @DisplayName("반복일정 잘못된 시간범위")
    @MethodSource("getInvalidRoutines")
    void invalidRoutine(OnboardingRequestDto.RoutineListRequest routine) {
        // when & then
        OnboardingException exception = assertThrows(OnboardingException.class,
                () -> onboardingService.saveRoutines(routine, testUser)
        );
        assertEquals(UserErrorStatus.INVALID_TIME_RANGE.getCode(), exception.getErrorReason().getCode());
        assertEquals(UserErrorStatus.INVALID_TIME_RANGE.getMessage(), exception.getErrorReason().getMessage());
    }

    @Test
    @DisplayName("반복일정 시간 충돌")
    void conflictRoutine() {
        // given
        OnboardingRequestDto.RoutineListRequest request = createRoutines(
                createRoutineDTO(Weekday.MONDAY, LocalTime.of(10, 0), LocalTime.of(14, 0), "틈틈 개발"),
                createRoutineDTO(Weekday.MONDAY, LocalTime.of(13, 0), LocalTime.of(17, 0), "틈틈 개발")
        );

        // when & then
        GlobalHandler exception = assertThrows(GlobalHandler.class,
                () -> onboardingService.saveRoutines(request, testUser)
        );
        assertEquals(UserErrorStatus.ROUTINE_TIME_CONFLICT.getCode(), exception.getErrorReason().getCode());
        assertEquals(UserErrorStatus.ROUTINE_TIME_CONFLICT.getMessage(), exception.getErrorReason().getMessage());
    }

    // ==================== 헬퍼 메서드 ====================
    // 수면패턴 생성 메서드
    private OnboardingRequestDto.SleepPatternRequest createSleepPattern(LocalTime sleepTime, LocalTime wakeTime) {
        return OnboardingRequestDto.SleepPatternRequest.builder()
                .sleepTime(sleepTime)
                .wakeTime(wakeTime)
                .build();
    }

    // 반복일정 생성 메서드
    // 정상 반복일정 생성 메서드
    private static List<OnboardingRequestDto.RoutineListRequest> getValidRoutines() {
        return List.of(
                createRoutines(createRoutineDTO(Weekday.MONDAY, LocalTime.of(9, 0), LocalTime.of(18, 0), "틈틈 개발")),
                createRoutines(createRoutineDTO(Weekday.TUESDAY, LocalTime.of(11, 0), LocalTime.of(19, 0), "틈틈 개발")),
                createRoutines(createRoutineDTO(Weekday.WEDNESDAY, LocalTime.of(20, 0), LocalTime.MIDNIGHT, "틈틈 개발")),
                createRoutines(createRoutineDTO(Weekday.THURSDAY, LocalTime.MIDNIGHT, LocalTime.of(4, 0), "틈틈 개발")),
                createRoutines(createRoutineDTO(Weekday.FRIDAY, LocalTime.MIDNIGHT, LocalTime.MIDNIGHT, "틈틈 개발"))
        );
    }

    // 비정상 반복일정 생성 메서드
    private static List<OnboardingRequestDto.RoutineListRequest> getInvalidRoutines() {
        return List.of(
                createRoutines(createRoutineDTO(Weekday.MONDAY, LocalTime.of(23, 0), LocalTime.of(2, 0), "잘못된 시간 범위")),
                createRoutines(createRoutineDTO(Weekday.TUESDAY, LocalTime.of(15, 0), LocalTime.of(10, 0), "잘못된 시간 범위")),
                createRoutines(createRoutineDTO(Weekday.WEDNESDAY, LocalTime.of(13, 0), LocalTime.of(13, 0), "잘못된 시간 범위"))
        );
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