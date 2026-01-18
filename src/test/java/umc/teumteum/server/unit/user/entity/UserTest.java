package umc.teumteum.server.unit.user.entity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import umc.teumteum.server.domain.user.entity.User;
import umc.teumteum.server.domain.user.entity.enums.UserStatus;

import static org.assertj.core.api.Assertions.assertThat;

class UserTest {

    @Test
    @DisplayName("첫 번째 정지 처분 시, 횟수가 1 증가하고 상태가 SUSPENDED가 되며 정지 기한이 설정된다")
    void suspend_first_time() {
        // given
        User user = User.builder()
                .status(UserStatus.ACTIVE)
                .suspensionCount(0)
                .build();

        // when
        user.suspend();

        // then
        assertThat(user.getSuspensionCount()).isEqualTo(1);
        assertThat(user.getStatus()).isEqualTo(UserStatus.SUSPENDED);
        assertThat(user.getSuspendedUntil()).isNotNull();
    }

    @Test
    @DisplayName("정지 횟수가 3회가 되는 순간, 상태는 BANNED가 되고 개인정보는 익명화 처리된다")
    void suspend_third_time_to_banned() {
        // given
        User user = User.builder()
                .id(1L)
                .nickname("지애님")
                .email("jiae@example.com")
                .status(UserStatus.SUSPENDED)
                .suspensionCount(2) // 이미 2회 정지된 상태
                .build();

        // when
        user.suspend();

        // then
        assertThat(user.getSuspensionCount()).isEqualTo(3);
        assertThat(user.getStatus()).isEqualTo(UserStatus.BANNED);
        assertThat(user.getNickname()).isNull();
        assertThat(user.getSocialId()).contains("banned-1-");
        assertThat(user.getEmail()).contains("@banned.local");
        assertThat(user.getProfileImageName()).isEqualTo(User.DEFAULT_PROFILE_IMAGE);
    }

    @Test
    @DisplayName("정지 상태인 유저를 활성화하면, 상태가 ACTIVE로 바뀌고 정지 기한이 초기화된다")
    void activate_suspended_user() {
        // given
        User user = User.builder()
                .status(UserStatus.SUSPENDED)
                .suspendedUntil(java.time.LocalDateTime.now())
                .build();

        // when
        user.activate();

        // then
        assertThat(user.getStatus()).isEqualTo(UserStatus.ACTIVE);
        assertThat(user.getSuspendedUntil()).isNull();
    }

    @Test
    @DisplayName("suspensionCount가 null인 신규 유저도 정지 처분 시 에러 없이 1회로 처리된다")
    void suspend_with_null_count() {
        // given
        User user = User.builder()
                .suspensionCount(null) // null 상태
                .build();

        // when
        user.suspend();

        // then
        assertThat(user.getSuspensionCount()).isEqualTo(1);
    }
}