package umc.teumteum.server.unit.user.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import umc.teumteum.server.domain.auth.dto.OAuthUserInfo;
import umc.teumteum.server.domain.user.entity.NotificationSetting;
import umc.teumteum.server.domain.user.entity.User;
import umc.teumteum.server.domain.user.entity.enums.SocialType;
import umc.teumteum.server.domain.user.repository.NotificationSettingRepository;
import umc.teumteum.server.domain.user.repository.UserRepository;
import umc.teumteum.server.domain.user.service.UserServiceImpl;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
@DisplayName("UserServiceImpl - User 관련 단위 테스트")
public class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private NotificationSettingRepository notificationSettingRepository;

    @InjectMocks
    private UserServiceImpl userService;

    private OAuthUserInfo userInfo;

    @BeforeEach
    void setUp() {
        userInfo = OAuthUserInfo.builder()
                .socialType(SocialType.KAKAO)
                .socialId("1234567890")
                .email("teumteum@kakao.com")
                .build();
    }

    // 회원 가입 시 알림 설정 생성 테스트
    @Test
    @DisplayName("사용자 생성 시 알림 설정이 기본값으로 저장되는지 확인")
    void findOrCreateUser_createNotificationSetting(){
        // given
        when(userRepository.findBySocialTypeAndSocialId(SocialType.KAKAO,"1234567890"))
                .thenReturn(Optional.empty());

        // when
        User result = userService.findOrCreateUser(userInfo);

        // then
        ArgumentCaptor<NotificationSetting> captor = ArgumentCaptor.forClass(NotificationSetting.class);
        verify(notificationSettingRepository).save(captor.capture());

        NotificationSetting savedSetting = captor.getValue();

        // 저장된 NotificationSetting의 user가 result인지 확인
        assertThat(savedSetting.getUser()).isEqualTo(result);

        // 기본값 확인
        assertThat(savedSetting.getFollow()).isTrue();
        assertThat(savedSetting.getTeum()).isTrue();
        assertThat(savedSetting.getRemindAlarm()).isTrue();
        assertThat(savedSetting.getTodayTodo()).isTrue();
    }

    @Test
    @DisplayName("findOrCreateUser 호출 시 기존 사용자면 NotificationSetting 생성하지 않음")
    void findOrCreateUser_existingUser() {
        // given: 이미 존재하는 사용자
        User existingUser = User.builder()
                .id(1L)
                .email("teumteum@kakao.com")
                .socialType(SocialType.KAKAO)
                .socialId("1234567890")
                .build();

        when(userRepository.findBySocialTypeAndSocialId(SocialType.KAKAO, "1234567890"))
                .thenReturn(Optional.of(existingUser));

        // when
        User result = userService.findOrCreateUser(userInfo);

        // then
        assertThat(result).isEqualTo(existingUser);

        // NotificationSettingRepository.save는 호출되지 않아야 함
        verify(notificationSettingRepository, times(0)).save(org.mockito.ArgumentMatchers.any(NotificationSetting.class));
    }

}