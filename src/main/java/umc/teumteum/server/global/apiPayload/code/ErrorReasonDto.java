package umc.teumteum.server.global.apiPayload.code;

import lombok.Builder;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@Builder
public class ErrorReasonDto {
  private HttpStatus httpStatus;

  private final Boolean isSuccess;
  private final String code;
  private final String message;

}
