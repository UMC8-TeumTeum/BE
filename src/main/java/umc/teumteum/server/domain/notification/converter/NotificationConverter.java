package umc.teumteum.server.domain.notification.converter;

import java.time.LocalDateTime;
import java.util.List;
import umc.teumteum.server.domain.notification.dto.NotificationResponseDto;
import umc.teumteum.server.domain.notification.dto.NotificationResponseDto.NotificationDto;
import umc.teumteum.server.domain.notification.entity.Notification;
import umc.teumteum.server.domain.user.entity.User;

public class NotificationConverter {

  public static NotificationResponseDto.NotificationDto toNotificationDto(Notification n, User friend, String profileImageUrl, LocalDateTime eventDate) {
    return NotificationResponseDto.NotificationDto.builder()
        .id(n.getId())
        .type(n.getType().name())
        .relatedId(n.getRelatedId())
        .content(n.getContent())
        .isRead(n.getIsRead()).eventDate(eventDate)
        .createdAt(n.getCreatedAt())
        .friendId(friend.getId())
        .friendNickname(friend.getNickname())
        .firendProfileImage(profileImageUrl)
        .build();
  }

  public static NotificationResponseDto.SliceResponseDto toSliceResponseDto(List<NotificationDto> notificationList, boolean hasNext, int page, int size) {
    return NotificationResponseDto.SliceResponseDto.builder()
        .content(notificationList)
        .hasNext(hasNext)
        .currentPage(page)
        .size(size)
        .build();
  }

  public static NotificationResponseDto.ReadResponseDto toReadResponseDto(Notification notification){
    return NotificationResponseDto.ReadResponseDto.builder()
            .notificationId(notification.getId())
            .isRead(notification.getIsRead())
            .build();
  }
}
