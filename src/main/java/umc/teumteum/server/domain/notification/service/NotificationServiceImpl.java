package umc.teumteum.server.domain.notification.service;

import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import umc.teumteum.server.domain.friend.repository.FriendRepository;
import umc.teumteum.server.domain.notification.converter.NotificationConverter;
import umc.teumteum.server.domain.notification.dto.NotificationResponseDto;
import umc.teumteum.server.domain.notification.entity.Notification;
import umc.teumteum.server.domain.notification.entity.enums.RelatedEntityType;
import umc.teumteum.server.domain.notification.repository.NotificationRepository;
import umc.teumteum.server.domain.teum.repository.TeumRequestRepository;
import umc.teumteum.server.domain.teum.repository.TeumResponseRepository;
import umc.teumteum.server.domain.user.entity.User;
import umc.teumteum.server.global.util.S3Util;

@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

  private final NotificationRepository notificationRepository;
  private final TeumResponseRepository teumResponseRepository;
  private final TeumRequestRepository teumRequestRepository;
  private final FriendRepository friendRepository;

  private final S3Util s3Util;

  @Override
  public List<NotificationResponseDto> getNotifications(User user) {

    List<Notification> notifications = notificationRepository.findByUserOrderByCreatedAtDesc(user); //User에 상대방 정보 담겨 있으니까.. 일단 상대방 정보를 기준으로 알림 조회
    Map<Long, User> relatedUserMap = resolveRelatedUsers(notifications);
    return notifications.stream()
        .map(n -> {
          User friend = relatedUserMap.get(n.getId());
          String profileImageUrl = s3Util.toPresignedUrl(friend.getProfileImageKey(), Duration.ofMinutes(30));
          return NotificationConverter.toDto(n, friend, profileImageUrl);
        })
        .toList();

  }

  private Map<Long, User> resolveRelatedUsers(List<Notification> notifications) {
    Map<Long,User> result = new HashMap<>();
    for (Notification notification : notifications) {
      RelatedEntityType type = notification.getType().getRelatedEntityType();
      Long relatedId = notification.getRelatedId();

      switch (type){
        case TEUM_RESPONSE, SCHEDULE -> {
          teumResponseRepository.findById(relatedId)
              .ifPresent(r -> result.put(notification.getId(), r.getReceiverUser()));
        }
        case TEUM_REQUEST -> {
          teumRequestRepository.findById(relatedId)
              .ifPresent(r -> result.put(notification.getId(), r.getUser()));
        }
        case FRIEND -> {
          friendRepository.findById(relatedId)
              .ifPresent(f -> result.put(notification.getId(), f.getFollower()));
        }
        default -> result.put(notification.getId(), null);
      }

    }
    return result;
  }
}
