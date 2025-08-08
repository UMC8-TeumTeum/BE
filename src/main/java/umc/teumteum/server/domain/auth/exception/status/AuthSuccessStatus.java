package umc.teumteum.server.domain.auth.exception.status;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;
import umc.teumteum.server.global.apiPayload.code.BaseCode;
import umc.teumteum.server.global.apiPayload.code.ReasonDto;

@Getter
@AllArgsConstructor
public enum AuthSuccessStatus implements BaseCode {

    SOCIAL_LOGIN_SUCCESS(HttpStatus.OK, "AUTH2001", "소셜 로그인이 완료되었습니다."),
    DEV_TOKEN_ISSUED(HttpStatus.OK, "AUTH2002", "개발용 액세스 토큰 발급이 완료되었습니다."),
    TOKEN_REISSUE_SUCCESS(HttpStatus.OK, "AUTH2003", "토큰 재발급이 완료되었습니다."),

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
