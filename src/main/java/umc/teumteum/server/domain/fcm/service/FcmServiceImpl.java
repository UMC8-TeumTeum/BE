package umc.teumteum.server.domain.fcm.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import umc.teumteum.server.domain.fcm.converter.FcmConverter;
import umc.teumteum.server.domain.fcm.entity.FcmToken;
import umc.teumteum.server.domain.fcm.repository.FcmTokenRepository;
import umc.teumteum.server.domain.user.entity.User;
import umc.teumteum.server.domain.user.repository.UserRepository;
import umc.teumteum.server.global.apiPayload.code.status.ErrorStatus;
import umc.teumteum.server.global.exception.handler.GlobalHandler;

@Service
@Transactional
@RequiredArgsConstructor
public class FcmServiceImpl implements FcmService{

  private final FcmTokenRepository fcmTokenRepository;
  private final UserRepository userRepository;

  @Override
  public void registerFcmToken(Long userId, String fcmToken) {
    User user = getUserOrThrow(userId);
    fcmTokenRepository.findByToken(fcmToken).ifPresentOrElse(
        existingToken -> {
          if (!Boolean.TRUE.equals(existingToken.getIsActive())) {
            existingToken.activate();}},
        () -> {
          FcmToken newToken = FcmConverter.toFcmToken(user, fcmToken);
          fcmTokenRepository.save(newToken);
        }
    );

  }

  @Override
  public void detachFcmToken(Long userId, String fcmToken) {
    User user = getUserOrThrow(userId);
    fcmTokenRepository.findByTokenAndUser(fcmToken, user).ifPresent(FcmToken::deactivate);
  }

  private User getUserOrThrow(Long userId) {
    return userRepository.findById(userId)
        .orElseThrow(() -> new GlobalHandler(ErrorStatus.INVALID_USER));
  }

}
