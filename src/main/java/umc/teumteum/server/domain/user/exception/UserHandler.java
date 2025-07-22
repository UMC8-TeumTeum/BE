package umc.teumteum.server.domain.user.exception;

import umc.teumteum.server.global.apiPayload.code.BaseErrorCode;
import umc.teumteum.server.global.exception.GeneralException;

public class UserHandler extends GeneralException {

  public UserHandler(BaseErrorCode code) {
    super(code);
  }
}
