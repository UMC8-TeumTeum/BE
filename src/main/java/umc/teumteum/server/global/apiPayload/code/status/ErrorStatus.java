package umc.teumteum.server.global.apiPayload.code.status;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;
import umc.teumteum.server.global.apiPayload.code.BaseErrorCode;
import umc.teumteum.server.global.apiPayload.code.ErrorReasonDto;

@Getter
@AllArgsConstructor
public enum ErrorStatus implements BaseErrorCode {
  _INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "COMMON500", "서버 에러, 관리자에게 문의 바랍니다."),
  _BAD_REQUEST(HttpStatus.BAD_REQUEST, "COMMON400", "잘못된 요청입니다."),
  _UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "COMMON401", "인증이 필요합니다."),
  _FORBIDDEN(HttpStatus.FORBIDDEN, "COMMON403", "금지된 요청입니다."),


  // 인증 관련
  MALFORMED_JWT_TOKEN(HttpStatus.BAD_REQUEST, "JWT4001", "잘못 구성된 JWT 형식입니다."),
  UNSUPPORTED_JWT_TOKEN(HttpStatus.BAD_REQUEST, "JWT4002", "지원하지 않는 JWT 형식입니다."),
  EMPTY_JWT_CLAIMS(HttpStatus.BAD_REQUEST, "JWT4003", "JWT 클레임이 비어 있습니다."),
  USER_NOT_FOUND(HttpStatus.UNAUTHORIZED, "JWT4011", "존재하지 않는 사용자입니다."),
  INVALID_JWT_SIGNATURE(HttpStatus.UNAUTHORIZED, "JWT4012", "유효하지 않은 JWT 서명입니다."),
  EXPIRED_JWT_TOKEN(HttpStatus.UNAUTHORIZED, "JWT4013", "JWT 토큰이 만료되었습니다."),
  INVALID_TOKEN_TYPE(HttpStatus.UNAUTHORIZED, "JWT4014", "유효하지 않은 토큰 타입입니다."),
  ACCESS_TOKEN_BLACKLISTED(HttpStatus.UNAUTHORIZED, "JWT4015", "블랙리스트된 액세스 토큰입니다."),
  INACTIVE_USER(HttpStatus.FORBIDDEN, "JWT4031", "비활성화된 사용자입니다."),


  // 동시성 관련
  LOCK_ACQUISITION_FAILED(HttpStatus.CONFLICT, "LOCK4091", "요청이 처리 중입니다. 잠시 후 다시 시도해주세요."),

  // Rate Limit 관련
  RATE_LIMIT_EXCEEDED(HttpStatus.TOO_MANY_REQUESTS, "RATE4291", "요청이 너무 많습니다. 잠시 후 다시 시도해주세요."),

  ;

  private final HttpStatus httpStatus;
  private final String code;
  private final String message;

  @Override
  public ErrorReasonDto getReason() {
    return ErrorReasonDto.builder()
            .isSuccess(true)
            .message(message)
            .code(code)
            .build();
  }

  @Override
  public ErrorReasonDto getReasonHttpStatus() {
    return ErrorReasonDto.builder()
            .httpStatus(httpStatus)
            .isSuccess(true)
            .code(code)
            .message(message)
            .build();
  }

}
