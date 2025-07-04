package umc.teumteum.server.global.exception.handler;

import umc.teumteum.server.global.apiPayload.code.BaseErrorCode;
import umc.teumteum.server.global.exception.GeneralException;

public class GlobalHandler extends GeneralException {

  public GlobalHandler(BaseErrorCode code) {
    super(code);
  }
}
