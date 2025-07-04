package umc.teumteum.server.global.apiPayload.code.status;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;
import umc.teumteum.server.global.apiPayload.code.BaseCode;
import umc.teumteum.server.global.apiPayload.code.ReasonDto;

@Getter
@AllArgsConstructor
public enum SuccessStatus implements BaseCode {
  _OK(HttpStatus.OK, "COMMON2000", "성공입니다.");

  private final HttpStatus httpStatus;
  private final String code;
  private final String message;


  @Override
  public ReasonDto getReason() {
    return ReasonDto.builder()
        .isSuccess(true)
        .message(message)
        .code(code)
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
