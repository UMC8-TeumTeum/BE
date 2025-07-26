package umc.teumteum.server.domain.fcm.service;

import umc.teumteum.server.domain.user.entity.User;

public interface FcmService {

  void registerFcmToken(User userId, String fcmToken);

  void detachFcmToken(User userId, String fcmToken);
}
