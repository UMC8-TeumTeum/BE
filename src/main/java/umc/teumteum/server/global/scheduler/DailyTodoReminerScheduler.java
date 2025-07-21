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
import umc.teumteum.server.domain.user.repository.UserRepository;
import umc.teumteum.server.global.notification.dto.NotificationPayload;
import umc.teumteum.server.global.notification.sender.FcmNotificationSender;
import umc.teumteum.server.domain.notification.entity.enums.NotificationType;

@Slf4j
@Component
@RequiredArgsConstructor
public class DailyTodoReminerScheduler {

  private final UserRepository userRepository;
  private final ScheduleRepository scheduleRepository;
  private final FcmNotificationSender fcmNotificationSender;
  private final FcmTokenRepository fcmTokenRepository;


  @Scheduled(cron = "0 0 9 * * *", zone = "Asia/Seoul")
//  @Scheduled(cron = "0 * * * * *", zone = "Asia/Seoul")
  public void sendDailyTodos() {
    log.info("[9am 스케줄러] 오늘의 투두 알림 메서드 호출");

    List<User> users = userRepository.findAll();
    for(User user : users) {
      List<Schedule> todos = scheduleRepository.findByUserIdAndDate(user.getId(), LocalDate.now());

      if (todos.isEmpty()) continue;

      DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");
      String content = todos.stream().map(
          todo -> {
            String time = todo.getStartTime().format(formatter);
            return time + " " + todo.getTitle();
          }).collect(Collectors.joining("\n"));

      NotificationPayload payload = NotificationPayload.builder()
          .title(NotificationType.DAILY_TODO.getTitle())
          .content(content)
          .type(NotificationType.DAILY_TODO)
          .data(Map.of("userId",user.getId().toString()))
          .build();

      List<FcmToken> tokens = fcmTokenRepository.findByUserAndIsActiveTrue(user);
      for(FcmToken token : tokens) {
        fcmNotificationSender.send(token.getToken(), payload);
      }


    }

  }


}
