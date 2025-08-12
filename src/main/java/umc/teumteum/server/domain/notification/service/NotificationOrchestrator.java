package umc.teumteum.server.domain.notification.service;

import jakarta.transaction.Transactional;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j; // ⬅️ 추가
import org.springframework.stereotype.Service;
import umc.teumteum.server.domain.fcm.entity.FcmToken;
import umc.teumteum.server.domain.fcm.repository.FcmTokenRepository;
import umc.teumteum.server.domain.notification.entity.Notification;
import umc.teumteum.server.domain.notification.entity.enums.NotificationType;
import umc.teumteum.server.domain.notification.repository.NotificationRepository;
import umc.teumteum.server.domain.user.entity.User;
import umc.teumteum.server.global.notification.dto.NotificationPayload;
import umc.teumteum.server.global.notification.sender.FcmNotificationSender;

@Service
@RequiredArgsConstructor
@Slf4j // ⬅️ 추가
public class NotificationOrchestrator {
  private final NotificationRepository notificationRepository;
  private final FcmTokenRepository fcmTokenRepository;
  private final FcmNotificationSender sender;

  // NotificationOrchestrator : Notification 저장, FCM 전송 로직 담당

  @Transactional
  public Long saveAndPush(User receiver, NotificationType type, String content, Long relatedId,
      Map<String,String> data) {

    // 1. Notification 생성 및 저장
    Notification notification = Notification.builder()
        .user(receiver)
        .type(type)
        .content(content)
        .relatedId(relatedId)
        .build();

    notificationRepository.save(notification);

    // 2. 활성화된 토큰 조회
    List<FcmToken> allTokens = fcmTokenRepository.findActiveTokensByUsers(List.of(receiver));

    // 3. Payload 생성 (기본값있으면 거기에 추가)
    Map<String, String> payloadData = new HashMap<>();
    if (data != null) payloadData.putAll(data);
    payloadData.putIfAbsent("type", type.name());
    payloadData.putIfAbsent("relatedId", String.valueOf(relatedId));
    payloadData.put("notificationId", String.valueOf(notification.getId()));
    payloadData.putIfAbsent("receiverId", String.valueOf(receiver.getId()));

    // 4. NotificationPayload 생성
    NotificationPayload payload = NotificationPayload.builder()
        .title(type.getTitle())
        .content(content)
        .type(type)
        .data(payloadData)
        .build();

    // 5. 전송. 실패해도 로그만 띄움
    for (FcmToken token : allTokens) {
      try {
        sender.send(token.getToken(), payload);
      } catch (Exception e) {
        log.warn("FCM send failed. token={}, notifId={}, type={}, relatedId={}, reason={}",
            token.getToken(), notification.getId(), type, relatedId, e.toString());
        // System.err.println("FCM send failed token=" + token.getToken()
        //     + ", notifId=" + notification.getId() + " : " + e.getMessage());
      }
    }

    return notification.getId();
  }

  @Transactional
  public List<Long> saveAndPushToMany(List<User> receivers,
      NotificationType type,
      String content,
      Long relatedId,
      Map<String, String> data) {

    // 1. Notification 생성 및 저장
    List<Notification> notifs = receivers.stream()
        .map(receiver -> Notification.builder()
            .user(receiver)
            .type(type)
            .content(content)
            .relatedId(relatedId)
            .build())
        .toList();

    notificationRepository.saveAll(notifs);

    // 2. 활성화된 토큰 조회
    List<FcmToken> tokens = fcmTokenRepository.findActiveTokensByUsers(receivers);

    // 3. json 데이터 생성
    Map<String, String> payloadData = new HashMap<>();
    if (data != null) payloadData.putAll(data);
    payloadData.putIfAbsent("type", type.name());
    payloadData.putIfAbsent("relatedId", String.valueOf(relatedId));

    // 4. NotificationPayload 생성
    NotificationPayload payload = NotificationPayload.builder()
        .title(type.getTitle())
        .content(content)
        .type(type)
        .data(payloadData)
        .build();

    // 5. 알람 전송 (실패 무시하고 로그만)
    tokens.forEach(t -> {
      try {
        sender.send(t.getToken(), payload);
      } catch (Exception e) {
        log.warn("FCM send failed (batch). token={}, type={}, relatedId={}, reason={}",
            t.getToken(), type, relatedId, e.toString());
        // System.err.println("FCM send failed token=" + t.getToken() + " : " + e.getMessage());
      }
    });

    return notifs.stream().map(Notification::getId).toList();
  }
}
