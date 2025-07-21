package umc.teumteum.server.domain.notification.service;

import java.util.List;
import umc.teumteum.server.domain.notification.dto.NotificationResponseDto;

public interface NotificationService {

  List<NotificationResponseDto> getNotifications(Long userId);
}
