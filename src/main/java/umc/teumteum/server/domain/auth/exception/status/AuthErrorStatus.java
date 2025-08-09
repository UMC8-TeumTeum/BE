package umc.teumteum.server.domain.auth.exception.status;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;
import umc.teumteum.server.global.apiPayload.code.BaseErrorCode;
import umc.teumteum.server.global.apiPayload.code.ErrorReasonDto;

@Getter
@AllArgsConstructor
public enum AuthErrorStatus implements BaseErrorCode {

    // Auth 도메인 인증 관련
    INVALID_SOCIAL_TYPE(HttpStatus.BAD_REQUEST, "AUTH4001", "지원하지 않는 소셜 로그인 타입입니다."),
    KAKAO_USER_INFO_FAILED(HttpStatus.BAD_REQUEST, "AUTH4002", "카카오 사용자 정보 조회에 실패했습니다."),
    NAVER_USER_INFO_FAILED(HttpStatus.BAD_REQUEST, "AUTH4003", "네이버 사용자 정보 조회에 실패했습니다."),
    INVALID_REFRESH_TOKEN(HttpStatus.BAD_REQUEST, "AUTH4004", "유효하지 않은 리프레시 토큰입니다."),
    REFRESH_TOKEN_NOT_FOUND(HttpStatus.UNAUTHORIZED, "AUTH4011", "리프레시 토큰이 존재하지 않습니다."),
    REFRESH_TOKEN_MISMATCH(HttpStatus.UNAUTHORIZED, "AUTH40112", "리프레시 토큰이 일치하지 않습니다."),


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
