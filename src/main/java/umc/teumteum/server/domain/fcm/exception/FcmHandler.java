package umc.teumteum.server.domain.fcm.exception;

import umc.teumteum.server.global.apiPayload.code.BaseErrorCode;
import umc.teumteum.server.global.exception.GeneralException;

public class FcmHandler extends GeneralException {

  public FcmHandler(BaseErrorCode code) {
    super(code);
  }
}
