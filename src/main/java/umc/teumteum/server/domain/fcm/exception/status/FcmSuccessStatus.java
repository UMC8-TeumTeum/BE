package umc.teumteum.server.domain.fcm.exception.status;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import umc.teumteum.server.global.apiPayload.code.BaseCode;
import umc.teumteum.server.global.apiPayload.code.ReasonDto;

@Getter
@RequiredArgsConstructor
public enum FcmSuccessStatus implements BaseCode {
  FCM_REGISTER_SUCCESS(HttpStatus.OK, "FCM2001", "FCM 토큰이 성공적으로 등록되었습니다."),
  FCM_DEACTIVATE_SUCCESS(HttpStatus.OK, "FCM2002", "FCM 토큰이 성공적으로 비활성화되었습니다."),
  FCM_SEND_SUCCESS(HttpStatus.OK, "FCM2003", "FCM 알림이 성공적으로 전송되었습니다."),

  ;

  private final HttpStatus httpStatus;
  private final String code;
  private final String message;

  @Override
  public ReasonDto getReason() {
    return ReasonDto.builder()
        .isSuccess(true)
        .code(code)
        .message(message)
        .build();
  }

  @Override
  public ReasonDto getReasonHttpStatus() {
    return ReasonDto.builder()
        .httpStatus(httpStatus)
        .isSuccess(true)
        .code(code)
        .message(message)
        .build();

  }
}
