package umc.teumteum.server.global.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import umc.teumteum.server.global.apiPayload.code.BaseErrorCode;
import umc.teumteum.server.global.apiPayload.code.ErrorReasonDto;

@Getter
@AllArgsConstructor
public class GeneralException extends RuntimeException {
  private BaseErrorCode code;

  public ErrorReasonDto getErrorReason() {
    return this.code.getReason();
  }

  public ErrorReasonDto getErrorReasonHttpStatus() {
    return this.code.getReasonHttpStatus();
  }

}
