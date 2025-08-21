//package umc.teumteum.server.unit.notification.service;
//
//
//import static org.mockito.Mockito.mock;
//import static org.mockito.Mockito.when;
//import static org.mockito.Mockito.withSettings;
//
//import org.junit.jupiter.api.AfterEach;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.DisplayName;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.extension.ExtendWith;
//import org.mockito.InjectMocks;
//import org.mockito.Mock;
//import org.mockito.MockedStatic;
//import org.mockito.Mockito;
//import org.mockito.junit.jupiter.MockitoExtension;
//import umc.teumteum.server.domain.friend.repository.FriendRepository;
//import umc.teumteum.server.domain.notification.converter.NotificationConverter;
//import umc.teumteum.server.domain.notification.entity.Notification;
//import umc.teumteum.server.domain.notification.entity.enums.RelatedEntityType;
//import umc.teumteum.server.domain.notification.repository.NotificationRepository;
//import umc.teumteum.server.domain.notification.service.NotificationService;
//import umc.teumteum.server.domain.teum.repository.TeumRequestRepository;
//import umc.teumteum.server.domain.teum.repository.TeumResponseRepository;
//import umc.teumteum.server.domain.user.entity.User;
//import umc.teumteum.server.global.util.S3Util;
//
//@ExtendWith(MockitoExtension.class)
//@DisplayName("NotificationServiceImpl - Notification 관련 서비스 메서드 단위 테스트")
//public class NotificationServiceTest {
//  @InjectMocks
//  private NotificationService notificationServiceImpl;
//
//  @Mock
//  private NotificationRepository notificationRepository;
//  @Mock
//  private TeumResponseRepository teumResponseRepository;
//  @Mock
//  private TeumRequestRepository teumRequestRepository;
//  @Mock
//  private FriendRepository friendRepository;
//
//  @Mock
//  private S3Util s3Util;
//
//  @Mock
//  private User testUser;
//
//  private MockedStatic<NotificationConverter> converterMock;
//
//  @BeforeEach
//  void setUp() {
//    converterMock = Mockito.mockStatic(NotificationConverter.class);
//
//  }
//
//  @AfterEach
//  void tearDown() {
//    converterMock.close();
//
//  }
//
//  @Test
//  @DisplayName("[getNotifications] - TC1 모든 타입 매핑, DTO 변환이 정상작동하는지 확인한다.")
//  void getNotifications_all_types_mapped() {
//    // given
//    Notification notification = mockNoti(31, RelatedEntityType.TEUM_REQUEST, 1);
//
//    // when
//
//    // then
//
//  }
//
//
//  @Test
//  @DisplayName("[getNotifications] - TC2 관련 엔티티가 없으면 해당 알림은 필터링 된다.")
//  void getNotifications_all_types_mapped() {
//    // given
//
//    // when
//
//    // then
//
//
//  }
//
//  @Test
//  @DisplayName("[getNotifications] - TC3 hasNext true/페이지/사이즈가 Converter로 그대로 전달된다. (페이징 정상 작동 확인)")
//  void getNotifications_all_types_mapped() {
//    // given
//
//    // when
//
//    // then
//
//
//  }
//
//  @Test
//  @DisplayName("[getNotifications] - TC4 S3 프리사인 URL 키와 만료시간 검증")
//  void getNotifications_all_types_mapped() {
//    // given
//
//    // when
//
//    // then
//
//
//  }
//
//  @Test
//  @DisplayName("[getNotifications] - TC5 알수 없는 타입은 제외된다.")
//  void getNotifications_all_types_mapped() {
//    // given
//
//    // when
//
//    // then
//
//
//  }
//
//  // 헬퍼 메서드
//  private Notification mockNoti(Long id, RelatedEntityType relationEntityType, Long relatedId) {
//    Notification notification = mock(Notification.class);
//    when(notification.getId()).thenReturn(id);
//
//    Object notificationType = mock(Object.class, withSettings().lenient());
//
//
//
//
//  }
//
//
//
//
//
//
//
//
//
//}
