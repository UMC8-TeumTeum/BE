package umc.teumteum.server.domain.notification.service;

import umc.teumteum.server.domain.notification.dto.NotificationResponseDto;
import umc.teumteum.server.domain.user.entity.User;

public interface NotificationService {


  NotificationResponseDto.SliceResponseDto getNotifications(User user, int page, int size);
}
