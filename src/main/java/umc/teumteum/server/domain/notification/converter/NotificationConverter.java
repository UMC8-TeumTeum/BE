package umc.teumteum.server.domain.notification.converter;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import umc.teumteum.server.domain.notification.dto.NotificationResponseDto;
import umc.teumteum.server.domain.notification.entity.Notification;
import umc.teumteum.server.domain.user.entity.User;
import umc.teumteum.server.global.util.S3Util;

public class NotificationConverter {

  public static NotificationResponseDto toDto(Notification n, User friend, String profileImageUrl) {
    return NotificationResponseDto.builder()
        .id(n.getId())
        .type(n.getType().name())
        .relatedId(n.getRelatedId())
        .content(n.getContent())
        .isRead(n.getIsRead())
        .createdAt(n.getCreatedAt())
        .friendId(friend.getId())
        .friendNickname(friend.getNickname())
        .firendProfileImage(profileImageUrl)
        .build();
  }
}
