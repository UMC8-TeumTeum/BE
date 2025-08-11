package umc.teumteum.server.domain.user.exception;

import umc.teumteum.server.global.apiPayload.code.BaseErrorCode;
import umc.teumteum.server.global.exception.GeneralException;

public class OnboardingException extends GeneralException {

  public OnboardingException(BaseErrorCode code) {
    super(code);
  }
}
