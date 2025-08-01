package umc.teumteum.server.unit.user.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import umc.teumteum.server.domain.home.repository.ScheduleRepository;
import umc.teumteum.server.domain.user.dto.OnboardingRequestDto;
import umc.teumteum.server.domain.user.entity.User;
import umc.teumteum.server.domain.user.entity.enums.SocialType;
import umc.teumteum.server.domain.user.entity.enums.UserStep;
import umc.teumteum.server.domain.user.entity.enums.Weekday;
import umc.teumteum.server.domain.user.exception.OnboardingHandler;
import umc.teumteum.server.domain.user.exception.status.UserErrorStatus;
import umc.teumteum.server.domain.user.repository.RoutineRepository;
import umc.teumteum.server.domain.user.service.OnboardingServiceImpl;
import umc.teumteum.server.global.exception.handler.GlobalHandler;
import umc.teumteum.server.global.util.TimeUtil;

import java.time.LocalTime;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("OnboardingService 테스트 - 수면패턴 x 반복일정 조합")
class OnboardingServiceTest {

    @InjectMocks
    private OnboardingServiceImpl onboardingService;

    @Mock
    private RoutineRepository routineRepository;

    @Mock
    private ScheduleRepository scheduleRepository;

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

    // ==================== 수면패턴A (22:00-07:00) ====================

    @Test
    @DisplayName("수면패턴A(22:00-07:00) + 반복일정A(09:00-18:00) - 정상")
    void sleepA_routineA_ok() {
        // when & then
        assertThatCode(() -> {
            onboardingService.saveSleepPattern(createSleepPatternA(), testUser);
            onboardingService.saveRoutines(createRoutineA(), testUser);
        }).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("수면패턴A(22:00-07:00) + 반복일정B(11:00-19:00) - 정상")
    void sleepA_routineB_ok() {
        // when & then
        assertThatCode(() -> {
            onboardingService.saveSleepPattern(createSleepPatternA(), testUser);
            onboardingService.saveRoutines(createRoutineB(), testUser);
        }).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("수면패턴A(22:00-07:00) + 반복일정C(20:00-00:00) - CONFLICT")
    void sleepA_routineC_conflict() {
        // when & then
        assertThatThrownBy(() -> {
            onboardingService.saveSleepPattern(createSleepPatternA(), testUser);
            onboardingService.saveRoutines(createRoutineC(), testUser);
        }).isInstanceOfSatisfying(GlobalHandler.class, ex -> {
            assertThat(ex.getCode()).isEqualTo(UserErrorStatus.ROUTINE_SLEEP_CONFLICT);
        });
    }

    @Test
    @DisplayName("수면패턴A(22:00-07:00) + 반복일정D(00:00-04:00) - CONFLICT")
    void sleepA_routineD_conflict() {
        // when & then
        assertThatThrownBy(() -> {
            onboardingService.saveSleepPattern(createSleepPatternA(), testUser);
            onboardingService.saveRoutines(createRoutineD(), testUser);
        }).isInstanceOfSatisfying(GlobalHandler.class, ex -> {
            assertThat(ex.getCode()).isEqualTo(UserErrorStatus.ROUTINE_SLEEP_CONFLICT);
        });
    }

    // ==================== 수면패턴B (18:00-00:00) ====================

    @Test
    @DisplayName("수면패턴B(18:00-00:00) + 반복일정A(09:00-18:00) - 정상")
    void sleepB_routineA_ok() {
        // when & then
        assertThatCode(() -> {
            onboardingService.saveSleepPattern(createSleepPatternB(), testUser);
            onboardingService.saveRoutines(createRoutineA(), testUser);
        }).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("수면패턴B(18:00-00:00) + 반복일정B(11:00-19:00) - CONFLICT")
    void sleepB_routineB_conflict() {
        // when & then
        assertThatThrownBy(() -> {
            onboardingService.saveSleepPattern(createSleepPatternB(), testUser);
            onboardingService.saveRoutines(createRoutineB(), testUser);
        }).isInstanceOfSatisfying(GlobalHandler.class, ex -> {
            assertThat(ex.getCode()).isEqualTo(UserErrorStatus.ROUTINE_SLEEP_CONFLICT);
        });
    }

    @Test
    @DisplayName("수면패턴B(18:00-00:00) + 반복일정C(20:00-00:00) - CONFLICT")
    void sleepB_routineC_conflict() {
        // when & then
        assertThatThrownBy(() -> {
            onboardingService.saveSleepPattern(createSleepPatternB(), testUser);
            onboardingService.saveRoutines(createRoutineC(), testUser);
        }).isInstanceOfSatisfying(GlobalHandler.class, ex -> {
            assertThat(ex.getCode()).isEqualTo(UserErrorStatus.ROUTINE_SLEEP_CONFLICT);
        });
    }

    @Test
    @DisplayName("수면패턴B(18:00-00:00) + 반복일정D(00:00-04:00) - 정상")
    void sleepB_routineD_ok() {
        // when & then
        assertThatCode(() -> {
            onboardingService.saveSleepPattern(createSleepPatternB(), testUser);
            onboardingService.saveRoutines(createRoutineD(), testUser);
        }).doesNotThrowAnyException();
    }

    // ==================== 수면패턴C (00:00-09:00) ====================

    @Test
    @DisplayName("수면패턴C(00:00-09:00) + 반복일정A(09:00-18:00) - 정상")
    void sleepC_routineA_ok() {
        // when & then
        assertThatCode(() -> {
            onboardingService.saveSleepPattern(createSleepPatternC(), testUser);
            onboardingService.saveRoutines(createRoutineA(), testUser);
        }).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("수면패턴C(00:00-09:00) + 반복일정B(11:00-19:00) - 정상")
    void sleepC_routineB_ok() {
        // when & then
        assertThatCode(() -> {
            onboardingService.saveSleepPattern(createSleepPatternC(), testUser);
            onboardingService.saveRoutines(createRoutineB(), testUser);
        }).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("수면패턴C(00:00-09:00) + 반복일정C(20:00-00:00) - 정상")
    void sleepC_routineC_ok() {
        // when & then
        assertThatCode(() -> {
            onboardingService.saveSleepPattern(createSleepPatternC(), testUser);
            onboardingService.saveRoutines(createRoutineC(), testUser);
        }).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("수면패턴C(00:00-09:00) + 반복일정D(00:00-04:00) - CONFLICT")
    void sleepC_routineD_conflict() {
        // when & then
        assertThatThrownBy(() -> {
            onboardingService.saveSleepPattern(createSleepPatternC(), testUser);
            onboardingService.saveRoutines(createRoutineD(), testUser);
        }).isInstanceOfSatisfying(GlobalHandler.class, ex -> {
            assertThat(ex.getCode()).isEqualTo(UserErrorStatus.ROUTINE_SLEEP_CONFLICT);
        });
    }

    // ==================== 수면패턴D (11:00-20:00) ====================

    @Test
    @DisplayName("수면패턴D(11:00-20:00) + 반복일정A(09:00-18:00) - CONFLICT")
    void sleepD_routineA_conflict() {
        // when & then
        assertThatThrownBy(() -> {
            onboardingService.saveSleepPattern(createSleepPatternD(), testUser);
            onboardingService.saveRoutines(createRoutineA(), testUser);
        }).isInstanceOfSatisfying(GlobalHandler.class, ex -> {
            assertThat(ex.getCode()).isEqualTo(UserErrorStatus.ROUTINE_SLEEP_CONFLICT);
        });
    }

    @Test
    @DisplayName("수면패턴D(11:00-20:00) + 반복일정B(11:00-19:00) - CONFLICT")
    void sleepD_routineB_conflict() {
        // when & then
        assertThatThrownBy(() -> {
            onboardingService.saveSleepPattern(createSleepPatternD(), testUser);
            onboardingService.saveRoutines(createRoutineB(), testUser);
        }).isInstanceOfSatisfying(GlobalHandler.class, ex -> {
            assertThat(ex.getCode()).isEqualTo(UserErrorStatus.ROUTINE_SLEEP_CONFLICT);
        });
    }

    @Test
    @DisplayName("수면패턴D(11:00-20:00) + 반복일정C(20:00-00:00) - 정상")
    void sleepD_routineC_ok() {
        // when & then
        assertThatCode(() -> {
            onboardingService.saveSleepPattern(createSleepPatternD(), testUser);
            onboardingService.saveRoutines(createRoutineC(), testUser);
        }).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("수면패턴D(11:00-20:00) + 반복일정D(00:00-04:00) - 정상")
    void sleepD_routineD_ok() {
        // when & then
        assertThatCode(() -> {
            onboardingService.saveSleepPattern(createSleepPatternD(), testUser);
            onboardingService.saveRoutines(createRoutineD(), testUser);
        }).doesNotThrowAnyException();
    }

    // ==================== 특이 케이스 ====================

    @Test
    @DisplayName("수면패턴 등록하지 않은 경우 반복일정 A~D - 정상")
    void no_sleep() {
        // when & then
        for (OnboardingRequestDto.RoutineListRequest routine : getAllRoutines()) {
            assertThatCode(() -> {
                onboardingService.saveRoutines(routine, testUser);
            }).doesNotThrowAnyException();
        }
    }

    @Test
    @DisplayName("반복일정E(00:00-00:00) - 24시간 일정")
    void test_routineE() {
        // when & then
        // 수면패턴 등록X인 경우 정상
        assertThatCode(() -> {
            onboardingService.saveRoutines(createRoutineE(), testUser);
        }).doesNotThrowAnyException();

        // 수면패턴 등록한 경우 예외
        for (OnboardingRequestDto.SleepPatternRequest sleepPattern : getAllSleepPatterns()) {
            assertThatThrownBy(() -> {
                onboardingService.saveSleepPattern(sleepPattern, testUser);
                onboardingService.saveRoutines(createRoutineE(), testUser);
            }).isInstanceOfSatisfying(GlobalHandler.class, ex -> {
                assertThat(ex.getCode()).isEqualTo(UserErrorStatus.ROUTINE_SLEEP_CONFLICT);
            });
        }
    }

    @Test
    @DisplayName("반복일정F(23:00-02:00) - 잘못된 시간범위")
    void test_routineF() {
        // when & then
        // 수면패턴 등록X인 경우 예외
        assertThatCode(() -> {
            onboardingService.saveRoutines(createRoutineF(), testUser);
        }).isInstanceOfSatisfying(OnboardingHandler.class, ex -> {
            assertThat(ex.getCode()).isEqualTo(UserErrorStatus.INVALID_TIME_RANGE);
        });

        // 수면패턴 등록한 경우 예외
        for (OnboardingRequestDto.SleepPatternRequest sleepPattern : getAllSleepPatterns()) {
            assertThatThrownBy(() -> {
                onboardingService.saveSleepPattern(sleepPattern, testUser);
                onboardingService.saveRoutines(createRoutineF(), testUser);
            }).isInstanceOfSatisfying(OnboardingHandler.class, ex -> {
                assertThat(ex.getCode()).isEqualTo(UserErrorStatus.INVALID_TIME_RANGE);
            });
        }
    }

    @Test
    @DisplayName("반복일정G(15:00-10:00) - 잘못된 시간범위")
    void test_routineG() {
        // when & then
        // 수면패턴 등록X인 경우 예외
        assertThatCode(() -> {
            onboardingService.saveRoutines(createRoutineG(), testUser);
        }).isInstanceOfSatisfying(OnboardingHandler.class, ex -> {
            assertThat(ex.getCode()).isEqualTo(UserErrorStatus.INVALID_TIME_RANGE);
        });

        // 수면패턴 등록한 경우 예외
        for (OnboardingRequestDto.SleepPatternRequest sleepPattern : getAllSleepPatterns()) {
            assertThatThrownBy(() -> {
                onboardingService.saveSleepPattern(sleepPattern, testUser);
                onboardingService.saveRoutines(createRoutineG(), testUser);
            }).isInstanceOfSatisfying(OnboardingHandler.class, ex -> {
                assertThat(ex.getCode()).isEqualTo(UserErrorStatus.INVALID_TIME_RANGE);
            });
        }
    }

    @Test
    @DisplayName("반복일정H(13:00-13:00) - 잘못된 시간범위")
    void test_routineH() {
        // when & then
        // 수면패턴 등록X인 경우 예외
        assertThatCode(() -> {
            onboardingService.saveRoutines(createRoutineH(), testUser);
        }).isInstanceOfSatisfying(OnboardingHandler.class, ex -> {
            assertThat(ex.getCode()).isEqualTo(UserErrorStatus.INVALID_TIME_RANGE);
        });

        // 수면패턴 등록한 경우 예외
        for (OnboardingRequestDto.SleepPatternRequest sleepPattern : getAllSleepPatterns()) {
            assertThatThrownBy(() -> {
                onboardingService.saveSleepPattern(sleepPattern, testUser);
                onboardingService.saveRoutines(createRoutineH(), testUser);
            }).isInstanceOfSatisfying(OnboardingHandler.class, ex -> {
                assertThat(ex.getCode()).isEqualTo(UserErrorStatus.INVALID_TIME_RANGE);
            });
        }
    }

    @Test
    @DisplayName("반복일정I(같은 요일 시간 겹침) - 반복일정 충돌")
    void test_routineI() {
        // when & then
        // 수면패턴 등록X인 경우 예외
        assertThatCode(() -> {
            onboardingService.saveRoutines(createRoutineI(), testUser);
        }).isInstanceOfSatisfying(GlobalHandler.class, ex -> {
            assertThat(ex.getCode()).isEqualTo(UserErrorStatus.ROUTINE_TIME_CONFLICT);
        });

        // 수면패턴 등록한 경우 예외
        for (OnboardingRequestDto.SleepPatternRequest sleepPattern : getAllSleepPatterns()) {
            assertThatThrownBy(() -> {
                onboardingService.saveSleepPattern(sleepPattern, testUser);
                onboardingService.saveRoutines(createRoutineI(), testUser);
            }).isInstanceOfSatisfying(GlobalHandler.class, ex -> {
                assertThat(ex.getCode()).isEqualTo(UserErrorStatus.ROUTINE_TIME_CONFLICT);
            });
        }
    }

    // ==================== 헬퍼 메서드 ====================

    // 수면패턴 생성 메서드
    private OnboardingRequestDto.SleepPatternRequest createSleepPatternA() {
        // A: 22:00-07:00
        return OnboardingRequestDto.SleepPatternRequest.builder()
                .sleepTime(LocalTime.of(22, 0))
                .wakeTime(LocalTime.of(7, 0))
                .build();
    }

    private OnboardingRequestDto.SleepPatternRequest createSleepPatternB() {
        // B: 18:00-00:00
        return OnboardingRequestDto.SleepPatternRequest.builder()
                .sleepTime(LocalTime.of(18, 0))
                .wakeTime(LocalTime.MIDNIGHT)
                .build();
    }

    private OnboardingRequestDto.SleepPatternRequest createSleepPatternC() {
        // C: 00:00-09:00
        return OnboardingRequestDto.SleepPatternRequest.builder()
                .sleepTime(LocalTime.MIDNIGHT)
                .wakeTime(LocalTime.of(9, 0))
                .build();
    }

    private OnboardingRequestDto.SleepPatternRequest createSleepPatternD() {
        // D: 11:00-20:00
        return OnboardingRequestDto.SleepPatternRequest.builder()
                .sleepTime(LocalTime.of(11, 0))
                .wakeTime(LocalTime.of(20, 0))
                .build();
    }

    // 반복일정 생성 메서드
    private OnboardingRequestDto.RoutineListRequest createRoutineA() {
        // A: 09:00-18:00
        List<OnboardingRequestDto.RoutineDTO> routines = Arrays.asList(
                createRoutineDTO(Weekday.MONDAY, LocalTime.of(9, 0), LocalTime.of(18, 0), "틈틈 개발")
        );
        return OnboardingRequestDto.RoutineListRequest.builder().routine(routines).build();
    }

    private OnboardingRequestDto.RoutineListRequest createRoutineB() {
        // B: 11:00-19:00
        List<OnboardingRequestDto.RoutineDTO> routines = Arrays.asList(
                createRoutineDTO(Weekday.MONDAY, LocalTime.of(11, 0), LocalTime.of(19, 0), "틈틈 개발")
        );
        return OnboardingRequestDto.RoutineListRequest.builder().routine(routines).build();
    }

    private OnboardingRequestDto.RoutineListRequest createRoutineC() {
        // C: 20:00-00:00
        List<OnboardingRequestDto.RoutineDTO> routines = Arrays.asList(
                createRoutineDTO(Weekday.MONDAY, LocalTime.of(20, 0), LocalTime.MIDNIGHT, "틈틈 개발")
        );
        return OnboardingRequestDto.RoutineListRequest.builder().routine(routines).build();
    }

    private OnboardingRequestDto.RoutineListRequest createRoutineD() {
        // D: 00:00-04:00
        List<OnboardingRequestDto.RoutineDTO> routines = Arrays.asList(
                createRoutineDTO(Weekday.TUESDAY, LocalTime.MIDNIGHT, LocalTime.of(4, 0), "틈틈 개발")
        );
        return OnboardingRequestDto.RoutineListRequest.builder().routine(routines).build();
    }

    private OnboardingRequestDto.RoutineListRequest createRoutineE() {
        // E: 00:00-00:00
        List<OnboardingRequestDto.RoutineDTO> routines = Arrays.asList(
                createRoutineDTO(Weekday.MONDAY, LocalTime.MIDNIGHT, LocalTime.MIDNIGHT, "24시간 일정 - 하루 안에서")
        );
        return OnboardingRequestDto.RoutineListRequest.builder().routine(routines).build();
    }

    private OnboardingRequestDto.RoutineListRequest createRoutineF() {
        // F: 예외 23:00-02:00
        List<OnboardingRequestDto.RoutineDTO> routines = Arrays.asList(
                createRoutineDTO(Weekday.MONDAY, LocalTime.of(23, 0), LocalTime.of(2, 0), "다음 날로 넘어감")
        );
        return OnboardingRequestDto.RoutineListRequest.builder().routine(routines).build();
    }

    private OnboardingRequestDto.RoutineListRequest createRoutineG() {
        // G: 예외 15:00-10:00
        List<OnboardingRequestDto.RoutineDTO> routines = Arrays.asList(
                createRoutineDTO(Weekday.MONDAY, LocalTime.of(15, 0), LocalTime.of(10, 0), "잘못된 시간 범위")
        );
        return OnboardingRequestDto.RoutineListRequest.builder().routine(routines).build();
    }

    private OnboardingRequestDto.RoutineListRequest createRoutineH() {
        // H: 예외 13:00-13:00
        List<OnboardingRequestDto.RoutineDTO> routines = Arrays.asList(
                createRoutineDTO(Weekday.MONDAY, LocalTime.of(13, 0), LocalTime.of(13, 0), "24시간 일정 - 다음 날로 넘어감")
        );
        return OnboardingRequestDto.RoutineListRequest.builder().routine(routines).build();
    }

    private OnboardingRequestDto.RoutineListRequest createRoutineI() {
        // I: 예외 반복일정 충돌
        List<OnboardingRequestDto.RoutineDTO> routines = Arrays.asList(
                createRoutineDTO(Weekday.MONDAY, LocalTime.of(10, 0), LocalTime.of(14, 0), "틈틈 개발"),
                createRoutineDTO(Weekday.MONDAY, LocalTime.of(13, 0), LocalTime.of(17, 0), "틈틈 개발")
        );
        return OnboardingRequestDto.RoutineListRequest.builder().routine(routines).build();
    }

    private OnboardingRequestDto.RoutineDTO createRoutineDTO(Weekday weekday, LocalTime startTime, LocalTime endTime, String title) {
        return OnboardingRequestDto.RoutineDTO.builder()
                .weekday(weekday)
                .startTime(startTime)
                .endTime(endTime)
                .title(title)
                .build();
    }

    private List<OnboardingRequestDto.SleepPatternRequest> getAllSleepPatterns() {
        return List.of(
                createSleepPatternA(),
                createSleepPatternB(),
                createSleepPatternC(),
                createSleepPatternD()
        );
    }

    private List<OnboardingRequestDto.RoutineListRequest> getAllRoutines() {
        return List.of(
                createRoutineA(),
                createRoutineB(),
                createRoutineC(),
                createRoutineD()
        );
    }
}