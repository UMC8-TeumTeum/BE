package umc.teumteum.server.domain.auth.exception;

import umc.teumteum.server.global.apiPayload.code.BaseErrorCode;
import umc.teumteum.server.global.exception.GeneralException;

public class AuthException extends GeneralException {

  public AuthException(BaseErrorCode code) {
    super(code);
  }
}
