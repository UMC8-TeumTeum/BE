package umc.teumteum.server.domain.fcm.service;

public interface FcmService {

  void registerFcmToken(Long userId, String fcmToken);

  void detachFcmToken(Long userId, String fcmToken);
}
