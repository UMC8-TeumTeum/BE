package umc.teumteum.server.global.notification.sender;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import umc.teumteum.server.global.notification.dto.NotificationPayload;

@Slf4j
@Component
@RequiredArgsConstructor
public class FcmNotificationSender {

  private final FirebaseMessaging firebaseMessaging;

  public void send(String targetToken, NotificationPayload payload) {
    Message message = Message.builder()
        .setToken(targetToken)
        .setNotification(Notification.builder()
            .setTitle(payload.getTitle())
            .setBody(payload.getContent())
            .build())
        .putAllData(payload.toFcmData())
        .build();
    log.info("FCM 전송: Message 생성 완료");
    log.info("targetToken: {}", targetToken);
    log.info("title: {}", payload.getTitle());
    log.info("body: {}", payload.getContent());
    log.info("data: {}", payload.toFcmData());

    try {
      firebaseMessaging.send(message);
    } catch (FirebaseMessagingException e) {
      log.warn(e.getMessage());
      log.warn("FCM 전송 실패: token={}, title={}, error={}", targetToken, payload.getTitle(), e.getMessage());
      // TODO : 예외처리 필요
    }

  }

}
