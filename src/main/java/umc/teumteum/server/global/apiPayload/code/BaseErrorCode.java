package umc.teumteum.server.global.apiPayload.code;

public interface BaseErrorCode {
  ErrorReasonDto getReason();
  ErrorReasonDto getReasonHttpStatus();

}
