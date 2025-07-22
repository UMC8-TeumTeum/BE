package umc.teumteum.server.domain.notification.converter;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import umc.teumteum.server.domain.notification.dto.NotificationResponseDto;
import umc.teumteum.server.domain.notification.entity.Notification;
import umc.teumteum.server.domain.user.entity.User;
import umc.teumteum.server.global.util.S3Util;

public class NotificationConverter {

  public static List<NotificationResponseDto> toNotificationResponse(
      List<Notification> notifications, Map<Long, User> relatedUserMap, S3Util s3Util) {

    return notifications.stream()
        .map(n -> {
          User friend = relatedUserMap.get(n.getId());

          return NotificationResponseDto.builder()
              .id(n.getId())
              .type(n.getType().name())
              .relatedId(n.getRelatedId())
              .content(n.getContent())
              .isRead(n.getIsRead())
              .createdAt(n.getCreatedAt())
              .friendId(friend.getId())
              .friendNickname(friend.getNickname())
              .firendProfileImage(s3Util.toPresignedUrl(friend.getProfileImageKey(),Duration.ofMinutes(30)))
              .build();
        })
        .toList();
  }
}
