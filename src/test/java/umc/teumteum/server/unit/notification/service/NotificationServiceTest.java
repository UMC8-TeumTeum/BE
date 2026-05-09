package umc.teumteum.server.unit.notification.service;


import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;
import umc.teumteum.server.domain.friend.entity.Friend;
import umc.teumteum.server.domain.friend.repository.FriendRepository;
import umc.teumteum.server.domain.notification.converter.NotificationConverter;
import umc.teumteum.server.domain.notification.dto.NotificationResponseDto;
import umc.teumteum.server.domain.notification.entity.Notification;
import umc.teumteum.server.domain.notification.entity.enums.NotificationType;
import umc.teumteum.server.domain.notification.repository.NotificationRepository;
import umc.teumteum.server.domain.notification.service.NotificationServiceImpl;
import umc.teumteum.server.domain.teum.entity.TeumRequest;
import umc.teumteum.server.domain.teum.entity.TeumResponse;
import umc.teumteum.server.domain.teum.repository.TeumRequestRepository;
import umc.teumteum.server.domain.teum.repository.TeumResponseRepository;
import umc.teumteum.server.domain.user.entity.User;
import umc.teumteum.server.global.util.S3Util;

import java.time.Duration;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("NotificationServiceImpl - Notification 관련 서비스 메서드 단위 테스트")
public class NotificationServiceTest {

  @InjectMocks
  private NotificationServiceImpl notificationServiceImpl;

  @Mock
  private NotificationRepository notificationRepository;
  @Mock
  private TeumResponseRepository teumResponseRepository;
  @Mock
  private TeumRequestRepository teumRequestRepository;
  @Mock
  private FriendRepository friendRepository;

  @Mock
  private S3Util s3Util;

  @Mock
  private User testUser;

  private MockedStatic<NotificationConverter> converterMock;

  @BeforeEach
  void setUp() {
    converterMock = Mockito.mockStatic(NotificationConverter.class);

  }

  @AfterEach
  void tearDown() {
    converterMock.close();

  }

  @Test
  @DisplayName("[getNotifications] - TC1 모든 타입 매핑, DTO 변환이 정상작동하는지 확인한다.")
  void all_types_mapped_and_converted() {
    // given
    int page = 1;
    int size = 10;

      // 알림 종류들 : RESPONSE, SCHEDULE(둘 다 TeumResponse), REQUEST(TeumRequest), FRIEND(Friend)
    Notification n1 = mockNoti(1L, NotificationType.TEUM_ACCEPTED, 101L); // TEUM_RESPONSE
    Notification n2 = mockNoti(2L, NotificationType.TEUM_CANCELED, 102L); // SCHEDULE
    Notification n3 = mockNoti(3L, NotificationType.TEUM_REQUEST, 103L); // TEUM_REQUEST
    Notification n4 = mockNoti(4L, NotificationType.FOLLOW, 104L); // FRIEND

    Pageable pageable = PageRequest.of(page - 1, size, Sort.by(Sort.Direction.DESC, "createdAt"));
    Slice<Notification> slices = new SliceImpl<>(List.of(n1, n2, n3, n4), pageable, false);
    when(notificationRepository.findByUserOrderByCreatedAtDesc(eq(testUser),
        any(Pageable.class))).thenReturn(slices);

      // 관련 유저들, 프로필 이미지 조회 부분 mocking
    User testFriend1 = mock(User.class); when(testFriend1.getProfileImageName()).thenReturn("tf1.png");
    User testFriend2 = mock(User.class); when(testFriend2.getProfileImageName()).thenReturn("tf2.png");
    User testFriend3 = mock(User.class); when(testFriend3.getProfileImageName()).thenReturn("tf3.png");
    User testFriend4 = mock(User.class); when(testFriend4.getProfileImageName()).thenReturn("tf4.png");

      // 관련 엔티티 mock, repodisoty stubbing
    TeumResponse teumResponse_related_accepted = mock(TeumResponse.class);
    when(teumResponse_related_accepted.getReceiverUser()).thenReturn(testFriend1);
    when(teumResponseRepository.findById(101L)).thenReturn(Optional.of(teumResponse_related_accepted));

    TeumResponse tuemResponse_related_canceled = mock(TeumResponse.class);
    when(tuemResponse_related_canceled.getReceiverUser()).thenReturn(testFriend2);
    when(teumResponseRepository.findById(102L)).thenReturn(Optional.of(tuemResponse_related_canceled));

    TeumRequest teumRequest = mock(TeumRequest.class);
    when(teumRequest.getUser()).thenReturn(testFriend3);
    when(teumRequestRepository.findById(103L)).thenReturn(Optional.of(teumRequest));

    Friend friend = mock(Friend.class);
    when(friend.getFollower()).thenReturn(testFriend4);
    when(friendRepository.findById(104L)).thenReturn(Optional.of(friend));

    when(s3Util.toPresignedUrl(startsWith("profile/"), any(Duration.class)))
        .thenReturn("https://presigned/mock/test");

      // NotificationResponseDto.NotificationDto mocking
    NotificationResponseDto.NotificationDto dto = mock(NotificationResponseDto.NotificationDto.class);
    converterMock.when(() -> NotificationConverter.toNotificationDto(any(), any(), any(), any()))
        .thenReturn(dto);

      // NotificatoinResponseDto.SliceResposneDto mocking
    NotificationResponseDto.SliceResponseDto expected = mock(NotificationResponseDto.SliceResponseDto.class);
    converterMock.when(() -> NotificationConverter.toSliceResponseDto(anyList(), eq(false), eq(page), eq(size))).thenReturn(expected);

    // when
    NotificationResponseDto.SliceResponseDto actualResponse = notificationServiceImpl.getNotifications(
        testUser, page, size);

    // then
      // 1. 통과 기준 : 타입별로 변환이 잘 되었는지 확인
    converterMock.verify(() -> NotificationConverter.toNotificationDto(eq(n1), eq(testFriend1), anyString(), any()));
    converterMock.verify(() -> NotificationConverter.toNotificationDto(eq(n2), eq(testFriend2), anyString(), any()));
    converterMock.verify(() -> NotificationConverter.toNotificationDto(eq(n3), eq(testFriend3), anyString(), any()));
    converterMock.verify(() -> NotificationConverter.toNotificationDto(eq(n4), eq(testFriend4), anyString(), any()));

      // 2. 통과 기준 : S3 presigned URL 호출 확인
    verify(s3Util, atLeast(4)).toPresignedUrl(startsWith("profile/"), eq(Duration.ofMinutes(30)));

  }

  @Test
  @DisplayName("[getNotifications] - TC2 관련 엔티티가 없으면 해당 알림은 필터링 된다.")
  void filter_when_related_not_found() {
    int page = 1, size = 10;

    Notification exist = mockNoti(1L, NotificationType.TEUM_REQUEST, 201L);
    Notification none  = mockNoti(2L, NotificationType.FOLLOW,       202L);

    Pageable pageable = PageRequest.of(page - 1, size, Sort.by(Sort.Direction.DESC, "createdAt"));
    Slice<Notification> slices = new SliceImpl<>(List.of(exist,none), pageable, false);
    when(notificationRepository.findByUserOrderByCreatedAtDesc(eq(testUser),
        any(Pageable.class))).thenReturn(slices);

    User testFriend = mock(User.class);
    when(testFriend.getProfileImageName()).thenReturn("tf1.png");
    TeumRequest tq = mock(TeumRequest.class);
    when(tq.getUser()).thenReturn(testFriend);

    when(teumRequestRepository.findById(201L)).thenReturn(Optional.of(tq));
    when(friendRepository.findById(202L)).thenReturn(Optional.empty());
    when(s3Util.toPresignedUrl(startsWith("profile/"), any(Duration.class)))
        .thenReturn("https://presigned/mock/test");

    NotificationResponseDto.NotificationDto dto = mock(NotificationResponseDto.NotificationDto.class);
    converterMock.when(() -> NotificationConverter.toNotificationDto(any(), any(), any(), any()))
        .thenReturn(dto);

    NotificationResponseDto.SliceResponseDto expected = mock(NotificationResponseDto.SliceResponseDto.class);
    converterMock.when(() -> NotificationConverter.toSliceResponseDto(
            argThat(list -> list.size() == 1), eq(false), eq(page), eq(size)))
        .thenReturn(expected);

    // when
    NotificationResponseDto.SliceResponseDto actual =
        notificationServiceImpl.getNotifications(testUser, page, size);

    // then
    assertSame(expected, actual);
      // 1. 통과 기준 : 필터링 되야 되는 none은 절대로 호출 안됨
    converterMock.verify(() -> NotificationConverter.toNotificationDto(eq(none), any(), any(), any()), never());
      // 2. 통과 기준 : 필터링 되면 안되는 exist는 호출됨
    converterMock.verify(() -> NotificationConverter.toNotificationDto(eq(exist), eq(testFriend), anyString(), any()));

  }

  @Test
  @DisplayName("[getNotifications] - TC3 hasNext, page, size 파라미터가 변환기에 그대로 전달")
  void hasNext_and_pagination_params() {
    int page = 3, size = 5;

    Notification n = mockNoti(1L, NotificationType.FOLLOW, 301L);
    Slice<Notification> slice = new SliceImpl<>(List.of(n), PageRequest.of(page-1, size), true);
    when(notificationRepository.findByUserOrderByCreatedAtDesc(eq(testUser), any(Pageable.class)))
        .thenReturn(slice);

    User testFriend = mock(User.class); when(testFriend.getProfileImageName()).thenReturn("tf1.png");
    Friend fr = mock(Friend.class); when(fr.getFollower()).thenReturn(testFriend);
    when(friendRepository.findById(301L)).thenReturn(Optional.of(fr));
    when(s3Util.toPresignedUrl(startsWith("profile/"), any(Duration.class)))
        .thenReturn("https://presigned/mock/test");

    NotificationResponseDto.NotificationDto dto = mock(NotificationResponseDto.NotificationDto.class);
    converterMock.when(() -> NotificationConverter.toNotificationDto(any(), any(), any(), any())).thenReturn(dto);

    NotificationResponseDto.SliceResponseDto expected = mock(NotificationResponseDto.SliceResponseDto.class);
    converterMock.when(() -> NotificationConverter.toSliceResponseDto(anyList(), eq(true), eq(page), eq(size)))
        .thenReturn(expected);

    // when
    NotificationResponseDto.SliceResponseDto actual =
        notificationServiceImpl.getNotifications(testUser, page, size);

    // then
    assertSame(expected, actual);

  }

  @Test
  @DisplayName("[getNotifications] - TC4 S3 presigned URL: 키와 만료시간 검증")
  void verify_presigned_url() {
    // given
    int page = 3, size = 5;

    Notification n = mockNoti(1L, NotificationType.TEUM_REQUEST, 401L);

    Pageable pageable = PageRequest.of(page - 1, size, Sort.by(Sort.Direction.DESC, "createdAt"));
    Slice<Notification> slices = new SliceImpl<>(List.of(n), pageable, false);
    when(notificationRepository.findByUserOrderByCreatedAtDesc(eq(testUser), any(Pageable.class))).thenReturn(slices);

    User testFriend = mock(User.class);
    when(testFriend.getProfileImageName()).thenReturn("tf1.png");
    TeumRequest tq = mock(TeumRequest.class); when(tq.getUser()).thenReturn(testFriend);
    when(teumRequestRepository.findById(401L)).thenReturn(Optional.of(tq));


    when(s3Util.toPresignedUrl(anyString(), any())).thenReturn("url");
    converterMock.when(() -> NotificationConverter.toNotificationDto(any(), any(), any(), any()))
        .thenReturn(mock(NotificationResponseDto.NotificationDto.class));
    converterMock.when(() -> NotificationConverter.toSliceResponseDto(anyList(), anyBoolean(), anyInt(), anyInt()))
        .thenReturn(mock(NotificationResponseDto.SliceResponseDto.class));

    // when
    notificationServiceImpl.getNotifications(testUser, page, size);

    // then
    ArgumentCaptor<String> keyCap = ArgumentCaptor.forClass(String.class);
    ArgumentCaptor<Duration> durCap = ArgumentCaptor.forClass(Duration.class);
    verify(s3Util).toPresignedUrl(keyCap.capture(), durCap.capture());

    assertEquals("profile/tf1.png", keyCap.getValue());
    assertEquals(Duration.ofMinutes(30), durCap.getValue());

  }


  @Test
  @DisplayName("[getNotifications] - TC5 TODO type의 경우 switch 문에서 default로 처리되야됨")
  void getNotifications_all_types_mapped() {
    // given
    int page = 1, size = 10;

    Notification todoNoti = mockNoti(1L, NotificationType.DAILY_TODO, 501L);

    Pageable pageable = PageRequest.of(page - 1, size, Sort.by(Sort.Direction.DESC, "createdAt"));
    Slice<Notification> slices = new SliceImpl<>(List.of(todoNoti), pageable, false);
    when(notificationRepository.findByUserOrderByCreatedAtDesc(eq(testUser), any(Pageable.class))).thenReturn(slices);

    converterMock.when(() -> NotificationConverter.toSliceResponseDto(
            argThat(List::isEmpty), eq(false), eq(1), eq(10)))
        .thenReturn(mock(NotificationResponseDto.SliceResponseDto.class));

    // when
    notificationServiceImpl.getNotifications(testUser, 1, 10);

    // then
    converterMock.verify(() -> NotificationConverter.toNotificationDto(any(), any(), any(), any()), never());
    verifyNoInteractions(teumRequestRepository, teumResponseRepository, friendRepository);

  }

  // 헬퍼 메서드
  private Notification mockNoti(Long id, NotificationType type, Long relatedId) {
    Notification notification = mock(Notification.class);
    when(notification.getId()).thenReturn(id);
    when(notification.getType()).thenReturn(type);
    when(notification.getRelatedId()).thenReturn(relatedId);
    return notification;

  }

}