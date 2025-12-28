package umc.teumteum.server.domain.notification.service;

import umc.teumteum.server.domain.notification.dto.NotificationResponseDto;
import umc.teumteum.server.domain.user.entity.User;

import java.util.List;

public interface NotificationService {


  NotificationResponseDto.SliceResponseDto getNotifications(User user, int page, int size);

  void updateDispatchStatus(List<Long> sentIds, List<Long> failIds);
}
