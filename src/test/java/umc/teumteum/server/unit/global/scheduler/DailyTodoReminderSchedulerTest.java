package umc.teumteum.server.unit.global.scheduler;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import umc.teumteum.server.domain.fcm.entity.FcmToken;
import umc.teumteum.server.domain.fcm.repository.FcmTokenRepository;
import umc.teumteum.server.domain.home.entity.Schedule;
import umc.teumteum.server.domain.home.entity.enums.ScheduleStatus;
import umc.teumteum.server.domain.home.entity.enums.ScheduleType;
import umc.teumteum.server.domain.home.repository.ScheduleRepository;
import umc.teumteum.server.domain.notification.entity.enums.NotificationType;
import umc.teumteum.server.domain.user.entity.User;
import umc.teumteum.server.domain.user.entity.enums.SocialType;
import umc.teumteum.server.domain.user.entity.enums.UserRole;
import umc.teumteum.server.domain.user.entity.enums.UserStatus;
import umc.teumteum.server.global.notification.dto.NotificationPayload;
import umc.teumteum.server.global.notification.sender.FcmNotificationSender;
import umc.teumteum.server.global.scheduler.DailyTodoReminerScheduler;
import umc.teumteum.server.support.RedisTestContainerSupport;

@ExtendWith(MockitoExtension.class)
@DisplayName("DailyTodoReminerScheduler - DailyTodoReminerScheduler 관련 단위 테스트")
public class DailyTodoReminderSchedulerTest extends RedisTestContainerSupport {
  @Mock
  private ScheduleRepository scheduleRepository;
  @Mock
  private FcmNotificationSender fcmNotificationSender;
  @Mock
  private FcmTokenRepository fcmTokenRepository;
  @InjectMocks
  private DailyTodoReminerScheduler scheduler;

  private List<User> users;
  private List<Schedule> schedules;
  private List<FcmToken> tokens;


  // 총 유저 3명 , 각 유저당 일정 3개, FCM 토큰 2개
  @BeforeEach
  void init() {
    users = new ArrayList<>();
    schedules = new ArrayList<>();
    tokens = new ArrayList<>();

    for (long i = 1; i <= 3; i++) {
      // 1. 유저 생성 (3번 유저만 비활성화, 나머지는 활성화)
      UserStatus status = (i == 3) ? UserStatus.INACTIVE : UserStatus.ACTIVE;
      User user = User.builder()
          .id(i)
          .socialId("social" + i)
          .socialType(SocialType.KAKAO)
          .email("user" + i + "@test.com")
          .nickname("user" + i)
          .role(UserRole.ROLE_USER)
          .status(status)
          .build();
      users.add(user);

      // 2. 유저당 일정 3개
      for (int j = 0; j < 3; j++) {
        Schedule schedule = Schedule.builder()
            .user(user)
            .title("일정 " + (j + 1) + " - user" + i)
            .description("테스트 일정입니다")
            .type(ScheduleType.TODO)
            .date(LocalDate.now())
            .startTime(LocalDateTime.of(LocalDate.now(), LocalTime.of(9 + j, 0)))
            .endTime(LocalDateTime.of(LocalDate.now(), LocalTime.of(10 + j, 0)))
            .isPublic(true)
            .includeTeum(false)
            .status(ScheduleStatus.ACTIVE)
//            .isDeleted(false)
            .build();
        schedules.add(schedule);
      }

      // 3. 유저당 FCM 토큰 2개
      for (int k = 0; k < 3; k++) {
        FcmToken token = FcmToken.builder()
            .user(user)
            .token("token" + k + "-user" + i)
            .isActive(true)
            .registeredAt(LocalDateTime.now())
            .build();
        tokens.add(token);
      }
    }
  }

  @Test
  @DisplayName("[DailyTodoReminerScheduler] - TC1 모든 유저에게 정상적으로 알림을 전송한다.")
  void DailyTodoReminerScheduler_TC1() {
    // given
    when(scheduleRepository.findAllByDateWithUser(LocalDate.now())).thenReturn(schedules);
    when(fcmTokenRepository.findActiveTokensByUsers(
        argThat(inputUsers -> hasSameUserIds(inputUsers, List.of(users.get(0),users.get(1))))
    )).thenReturn(
        tokens.stream()
            .filter(t -> {
              Long uid = t.getUser().getId();
              return uid.equals(1L) || uid.equals(2L);
            })
            .toList()
    );

    // when
    scheduler.sendDailyTodos();

    // then
    verify(fcmNotificationSender,times(6)).send(anyString(), any(NotificationPayload.class));

  }


  @Test
  @DisplayName("[DailyTodoReminerScheduler] - TC2 토큰이 없는 경우 알림을 보내지 않는다.")
  void DailyTodoReminerScheduler_TC2() {
    // given
    when(scheduleRepository.findAllByDateWithUser(LocalDate.now())).thenReturn(schedules);
    when(fcmTokenRepository.findActiveTokensByUsers(
        argThat(inputUsers -> hasSameUserIds(inputUsers, List.of(users.get(0), users.get(1))))
    )).thenReturn(List.of());

    // when
    scheduler.sendDailyTodos();

    // then
    verify(fcmNotificationSender, never()).send(any(), any());


  }
  @Test
  @DisplayName("[DailyTodoReminerScheduler] - TC3 일정이 아예 없는 경우 알림을 보내지 않는다.")
  void DailyTodoReminerScheduler_TC3() {
    // given
    when(scheduleRepository.findAllByDateWithUser(LocalDate.now())).thenReturn(List.of());

    // when
    scheduler.sendDailyTodos();

    // then
    verifyNoInteractions(fcmNotificationSender);


  }
  @Test
  @DisplayName("[DailyTodoReminerScheduler] - TC4 비활성 유저는 알림을 보내지 않는다.")
  void DailyTodoReminderScheduler_TC4() {
    // given
    when(scheduleRepository.findAllByDateWithUser(LocalDate.now())).thenReturn(schedules);
    // 토큰 조회시, ACTIVE 유저의 토큰만 조회됨
    when(fcmTokenRepository.findActiveTokensByUsers(
        argThat(inputUsers -> hasSameUserIds(inputUsers, List.of(users.get(0),users.get(1))))
    )).thenReturn(
        tokens.stream()
            .filter(t -> {
              Long uid = t.getUser().getId();
              return uid.equals(1L) || uid.equals(2L);
            })
            .toList()
    );
    // when
    scheduler.sendDailyTodos();

    // then
    // ACTIVE 유저의 토큰 6개 호출
    verify(fcmNotificationSender, times(6)).send(anyString(), any());
    // INACTIVE 유저의 토큰은 호출되지 않아야 함.
    tokens.stream()
        .filter(t->!t.getUser().getStatus().equals(UserStatus.ACTIVE))
        .forEach(token -> verify(fcmNotificationSender, never()).send(eq(token.getToken()),any()));

  }
  @Test
  @DisplayName("[DailyTodoReminerScheduler] - TC5 유저별 일정이 startTime 순으로 정렬되어 있는지 검증한다.")
  void DailyTodoReminderScheduler_TC5() {
    // given
    when(scheduleRepository.findAllByDateWithUser(LocalDate.now())).thenReturn(schedules);
    when(fcmTokenRepository.findActiveTokensByUsers(any())).thenReturn(tokens);

    // when
    scheduler.sendDailyTodos();

    // then
    long expectedSendCount = tokens.stream()
        .filter(t -> t.getUser().getStatus() == UserStatus.ACTIVE)
        .count();

    verify(fcmNotificationSender, times((int) expectedSendCount)).send(anyString(), argThat(payload -> {
      String content = payload.getContent(); // 예: "09:00 일정1\n10:00 일정2\n11:00 일정3"
      String[] lines = content.split("\n");

      List<LocalTime> times = new ArrayList<>();
      for (String line : lines) {
        String timeStr = line.split(" ")[0]; // "09:00 일정1" → "09:00"
        times.add(LocalTime.parse(timeStr)); // HH:mm 형식으로 LocalTime 변환
      }

      return isSorted(times);
    }));
  }

  @Test
  @DisplayName("[DailyTodoReminerScheduler] - TC6 payload 내용(title, content, type, data.userId)을 검증한다.")
  void DailyTodoReminderScheduler_TC6() {
    // given
    when(scheduleRepository.findAllByDateWithUser(LocalDate.now())).thenReturn(schedules);
    when(fcmTokenRepository.findActiveTokensByUsers(any())).thenReturn(tokens);

    // when
    scheduler.sendDailyTodos();

    // then
    long expectedSendCount = tokens.stream()
        .filter(t -> t.getUser().getStatus() == UserStatus.ACTIVE)
        .count();

    // ArgumentCaptor로 실제 호출된 payload들 전부 캡처
    ArgumentCaptor<NotificationPayload> payloadCaptor = ArgumentCaptor.forClass(NotificationPayload.class);
    verify(fcmNotificationSender, times((int) expectedSendCount)).send(anyString(), payloadCaptor.capture());

    List<NotificationPayload> payloads = payloadCaptor.getAllValues();

    for (NotificationPayload payload : payloads) {
      // 1. title 확인
      assert payload.getTitle().equals(NotificationType.DAILY_TODO.getTitle());

      // 2. type 확인
      assert payload.getType() == NotificationType.DAILY_TODO;

      // 3. data.userId 확인
      assert payload.getData() != null;
      assert payload.getData().containsKey("userId");
      assert payload.getData().get("userId").matches("\\d+");

      // 4. content 확인
      assert payload.getContent() != null;
      assert !payload.getContent().isBlank();
    }
  }



  private boolean hasSameUserIds(List<User> actual, List<User> expected) {
    Set<Long> actualIds = actual.stream().map(User::getId).collect(Collectors.toSet());
    Set<Long> expectedIds = expected.stream().map(User::getId).collect(Collectors.toSet());
    return actualIds.equals(expectedIds);
  }

  private boolean isSorted(List<LocalTime> times) {
    for (int i = 1; i < times.size(); i++) {
      if (times.get(i - 1).isAfter(times.get(i))) {
        return false;
      }
    }
    return true;
  }
}
