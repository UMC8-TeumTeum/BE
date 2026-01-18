package umc.teumteum.server.unit.global.scheduler;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import umc.teumteum.server.domain.user.entity.User;
import umc.teumteum.server.domain.user.entity.enums.UserStatus;
import umc.teumteum.server.domain.user.repository.UserRepository;
import umc.teumteum.server.global.scheduler.UserSuspendScheduler;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class UserSuspendSchedulerTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserSuspendScheduler userSuspendScheduler;

    @Test
    @DisplayName("정지 기간이 지난 유저 리스트를 가져와서 모두 ACTIVE로 변경한다")
    void activate_expired_users_logic() {
        // given: 정지 기간이 지난 가짜 유저 생성
        User user = User.builder()
                .status(UserStatus.SUSPENDED)
                .suspendedUntil(LocalDateTime.now().minusDays(1))
                .build();

        given(userRepository.findAllByStatusAndSuspendedUntilBefore(eq(UserStatus.SUSPENDED), any(LocalDateTime.class)))
                .willReturn(List.of(user));

        // when: 스케줄러 실행
        userSuspendScheduler.activateExpiredUsersAtMidnight();

        // then: 유저 상태가 ACTIVE로 변했는지 검증
        assertThat(user.getStatus()).isEqualTo(UserStatus.ACTIVE);
        assertThat(user.getSuspendedUntil()).isNull();
    }
}