package umc.teumteum.server.domain.notification.service;

import java.util.List;
import umc.teumteum.server.domain.notification.dto.NotificationResponseDto;
import umc.teumteum.server.domain.user.entity.User;

public interface NotificationService {

  List<NotificationResponseDto> getNotifications(User user);
}
