package umc.teumteum.server.domain.auth.exception;

import umc.teumteum.server.global.apiPayload.code.BaseErrorCode;
import umc.teumteum.server.global.exception.GeneralException;

public class AuthHandler extends GeneralException {

  public AuthHandler(BaseErrorCode code) {
    super(code);
  }
}
