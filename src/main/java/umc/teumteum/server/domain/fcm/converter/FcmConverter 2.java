package umc.teumteum.server.domain.fcm.converter;

import java.time.LocalDateTime;
import umc.teumteum.server.domain.fcm.entity.FcmToken;
import umc.teumteum.server.domain.user.entity.User;

public class FcmConverter {

  public static FcmToken toFcmToken(User user, String token) {
    return FcmToken.builder()
        .token(token)
        .user(user)
        .registeredAt(LocalDateTime.now())
        .isActive(true)
        .build();
  }

}
