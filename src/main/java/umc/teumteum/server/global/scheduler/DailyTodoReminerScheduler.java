package umc.teumteum.server.global.scheduler;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import umc.teumteum.server.domain.fcm.entity.FcmToken;
import umc.teumteum.server.domain.fcm.repository.FcmTokenRepository;
import umc.teumteum.server.domain.home.entity.Schedule;
import umc.teumteum.server.domain.home.repository.ScheduleRepository;
import umc.teumteum.server.domain.user.entity.User;
import umc.teumteum.server.domain.user.entity.enums.UserStatus;
import umc.teumteum.server.domain.user.repository.UserRepository;
import umc.teumteum.server.global.notification.dto.NotificationPayload;
import umc.teumteum.server.global.notification.sender.FcmNotificationSender;
import umc.teumteum.server.domain.notification.entity.enums.NotificationType;

@Slf4j
@Component
@RequiredArgsConstructor
public class DailyTodoReminerScheduler {

  private final ScheduleRepository scheduleRepository;
  private final FcmNotificationSender fcmNotificationSender;
  private final FcmTokenRepository fcmTokenRepository;


  @Scheduled(cron = "0 0 9 * * *", zone = "Asia/Seoul")
//  @Scheduled(cron = "0 * * * * *", zone = "Asia/Seoul")
  public void sendDailyTodos() {
    log.info("[9am 스케줄러] 오늘의 투두 알림 메서드 호출");

    LocalDate today = LocalDate.now();
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");

    // 1. 유저 정보가 있는 Schedule 한번에 가져옴
    List<Schedule> schedules = scheduleRepository.findAllByDateWithUser(today);
    // 2. 유저 별로 스케줄 묶기
    Map<User, List<Schedule>> scheduleMap = schedules.stream().collect(Collectors.groupingBy(Schedule::getUser));
    // 3. 활성화 상태의 유저 리스트 추출
    List<User> users = List.copyOf(scheduleMap.keySet());
    List<User> activeUsers = users.stream().filter(user -> user.getStatus() == UserStatus.ACTIVE)
        .toList();
    // 4. 유저 토큰 조회
    List<FcmToken> allTokens = fcmTokenRepository.findActiveTokensByUsers(activeUsers);
    Map<Long, List<FcmToken>> tokenMap = allTokens.stream()
        .collect(Collectors.groupingBy(token -> token.getUser().getId()));


    // 5. 유저별 알림 전송
    for(User user : activeUsers) {
      List<Schedule> userSchedules = scheduleMap.get(user).stream()
          .sorted((s1, s2) -> s1.getStartTime().compareTo(s2.getStartTime()))
          .toList();

      String content = userSchedules.stream()
          .map(s->s.getStartTime().format(formatter)+" "+s.getTitle())
          .collect(Collectors.joining("\n"));

      NotificationPayload payload = NotificationPayload.builder()
          .title(NotificationType.DAILY_TODO.getTitle())
          .content(content)
          .type(NotificationType.DAILY_TODO)
          .data(Map.of("userId",user.getId().toString()))
          .build();

      // * 활성화 토큰이 없을 경우를 대비해서.. 없을 경우 List.of()처리
      List<FcmToken> tokens = tokenMap.getOrDefault(user.getId(), List.of());
      for (FcmToken token : tokens) {
        fcmNotificationSender.send(token.getToken(), payload);
      }

    }

  }


}
