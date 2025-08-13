package umc.teumteum.server.domain.user.exception;

import umc.teumteum.server.global.apiPayload.code.BaseErrorCode;
import umc.teumteum.server.global.exception.GeneralException;

public class UserException extends GeneralException {

  public UserException(BaseErrorCode code) {
    super(code);
  }
}
