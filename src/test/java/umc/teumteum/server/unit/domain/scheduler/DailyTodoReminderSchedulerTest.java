package umc.teumteum.server.unit.domain.scheduler;

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
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import umc.teumteum.server.domain.fcm.entity.FcmToken;
import umc.teumteum.server.domain.fcm.repository.FcmTokenRepository;
import umc.teumteum.server.domain.home.entity.Schedule;
import umc.teumteum.server.domain.home.entity.enums.ScheduleStatus;
import umc.teumteum.server.domain.home.entity.enums.ScheduleType;
import umc.teumteum.server.domain.home.repository.ScheduleRepository;
import umc.teumteum.server.domain.user.entity.User;
import umc.teumteum.server.domain.user.entity.enums.SocialType;
import umc.teumteum.server.domain.user.entity.enums.UserRole;
import umc.teumteum.server.domain.user.entity.enums.UserStatus;
import umc.teumteum.server.global.notification.dto.NotificationPayload;
import umc.teumteum.server.global.notification.sender.FcmNotificationSender;
import umc.teumteum.server.global.scheduler.DailyTodoReminerScheduler;

@ExtendWith(MockitoExtension.class)
public class DailyTodoReminderSchedulerTest {
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
            .isDeleted(false)
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

  private boolean hasSameUserIds(List<User> actual, List<User> expected) {
    Set<Long> actualIds = actual.stream().map(User::getId).collect(Collectors.toSet());
    Set<Long> expectedIds = expected.stream().map(User::getId).collect(Collectors.toSet());
    return actualIds.equals(expectedIds);
  }
}
