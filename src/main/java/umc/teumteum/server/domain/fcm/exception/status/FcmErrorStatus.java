package umc.teumteum.server.domain.fcm.exception.status;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import umc.teumteum.server.global.apiPayload.code.BaseErrorCode;
import umc.teumteum.server.global.apiPayload.code.ErrorReasonDto;

@Getter
@RequiredArgsConstructor
public enum FcmErrorStatus implements BaseErrorCode {
  FCM_BAD_REQUEST(HttpStatus.BAD_REQUEST, "FCM4001", "존재하지 않는 FCM 토큰입니다."),
  FCM_ALREADY_DEACTIVATED(HttpStatus.BAD_REQUEST, "FCM4002","이미 비활성화된 FCM 토큰입니다."),

  ;

  private final HttpStatus httpStatus;
  private final String code;
  private final String message;

  @Override
  public ErrorReasonDto getReason() {
    return ErrorReasonDto.builder()
        .isSuccess(false)
        .message(message)
        .code(code)
        .build();
  }

  @Override
  public ErrorReasonDto getReasonHttpStatus() {
    return ErrorReasonDto.builder()
        .httpStatus(httpStatus)
        .isSuccess(false)
        .code(code)
        .message(message)
        .build();
  }
}
