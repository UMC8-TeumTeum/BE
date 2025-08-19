package umc.teumteum.server.unit.fcm.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import umc.teumteum.server.domain.fcm.converter.FcmConverter;
import umc.teumteum.server.domain.fcm.entity.FcmToken;
import umc.teumteum.server.domain.fcm.repository.FcmTokenRepository;
import umc.teumteum.server.domain.fcm.service.FcmServiceImpl;
import umc.teumteum.server.domain.user.entity.User;

@ExtendWith(MockitoExtension.class)
@DisplayName("FcmServiceImpl - Fcm Token 관련 서비스 메서드 단위 테스트")
public class FcmServiceTest {
  @Mock
  private FcmTokenRepository fcmTokenRepository;

  @InjectMocks
  private FcmServiceImpl fcmService;

  @Mock
  private User testUser;

  private MockedStatic<FcmConverter> converterMock;

  @BeforeEach
  void setUp() {
    converterMock = Mockito.mockStatic(FcmConverter.class);

  }

  @AfterEach
  void tearDown() {
    converterMock.close();
  }

  @Test
  @DisplayName("[registerFcmToken] - TC1 기존에 활성화된 FCM 토큰이 존재하면 아무 작업도 하지 않는다.")
  void register_fcm_existing_active_token() {
    //given
    String token = "thisismockfcmtoken";
    FcmToken existingToken = mock(FcmToken.class);

    when(fcmTokenRepository.findByToken(token)).thenReturn(Optional.of(existingToken));
    when(existingToken.getIsActive()).thenReturn(true);

    //when
    fcmService.registerFcmToken(testUser, token);

    //then
    verify(existingToken, never()).activate();
    verify(fcmTokenRepository, never()).save(any());



  }
  @Test
  @DisplayName("[registerFcmToken] - TC2 기존에 비활성화된 FCM 토큰이 존재하면 활성화한다.")
  void register_fcm_existing_deactive_token() {
    //given
    String token = "thisismockfcmtoken";
    FcmToken existingToken = mock(FcmToken.class);

    when(fcmTokenRepository.findByToken(token)).thenReturn(Optional.of(existingToken));
    when(existingToken.getIsActive()).thenReturn(false);

    //when
    fcmService.registerFcmToken(testUser, token);

    //then
    verify(existingToken).activate();
    verify(fcmTokenRepository, never()).save(any());
  }

  @Test
  @DisplayName("[registerFcmToken] - TC3 해당 FCM 토큰이 존재하지 않으면 새로 등록한다.")
  void register_fcm_save_new_token() {
    //given
    String token = "thisismockfcmtoken";
    FcmToken newToken = mock(FcmToken.class);

    when(fcmTokenRepository.findByToken(token)).thenReturn(Optional.empty());
    converterMock.when(() -> FcmConverter.toFcmToken(testUser, token)).thenReturn(newToken);

    //when
    fcmService.registerFcmToken(testUser, token);

    //then
    verify(fcmTokenRepository).save(newToken);

  }






}
