package umc.teumteum.server.domain.fcm.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import umc.teumteum.server.domain.fcm.converter.FcmConverter;
import umc.teumteum.server.domain.fcm.entity.FcmToken;
import umc.teumteum.server.domain.fcm.repository.FcmTokenRepository;
import umc.teumteum.server.domain.user.entity.User;

@Service
@Transactional
@RequiredArgsConstructor
public class FcmServiceImpl implements FcmService{

  private final FcmTokenRepository fcmTokenRepository;

  @Override
  public void registerFcmToken(User user, String fcmToken) {
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
  public void detachFcmToken(User user, String fcmToken) {
    fcmTokenRepository.findByTokenAndUser(fcmToken, user).ifPresent(FcmToken::deactivate);
  }

}
