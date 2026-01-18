package umc.teumteum.server.unit.teum.converter;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import umc.teumteum.server.domain.teum.converter.TeumConverter;
import umc.teumteum.server.domain.teum.dto.common.ParticipantDto;
import umc.teumteum.server.domain.user.entity.User;
import umc.teumteum.server.domain.user.entity.enums.UserStatus;
import umc.teumteum.server.global.util.TimeUtil;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class TeumConverterTest {

    @Mock
    private TimeUtil timeUtil;

    @InjectMocks
    private TeumConverter teumConverter;

    @Test
    @DisplayName("정지된 사용자의 닉네임은 '정지된 사용자'로 마스킹되고 ID는 null이 된다")
    void masking_test() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        // given
        User suspendedUser = User.builder()
                .status(UserStatus.SUSPENDED)
                .nickname("나쁜사람")
                .build();

        // when
        Method method = TeumConverter.class.getDeclaredMethod("toParticipantDto", User.class, String.class);
        method.setAccessible(true);
        ParticipantDto result = (ParticipantDto) method.invoke(teumConverter, suspendedUser, "some-url");

        // then
        assertThat(result.getNickname()).isEqualTo("정지된 사용자");
        assertThat(result.getUserId()).isNull();
        assertThat(result.getProfileImageUrl()).isEqualTo(User.DEFAULT_PROFILE_IMAGE);
    }
}