package umc.teumteum.server.domain.user.exception.status;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;
import umc.teumteum.server.global.apiPayload.code.BaseErrorCode;
import umc.teumteum.server.global.apiPayload.code.ErrorReasonDto;

@Getter
@AllArgsConstructor
public enum UserErrorStatus implements BaseErrorCode {

    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "USER4040", "존재하지 않는 사용자입니다."),


    // 사용자 온보딩
    TOS_CONSENT_NOT_AGREED(HttpStatus.BAD_REQUEST, "ONBOARDING4001", "서비스 이용약관은 필수 동의 항목입니다."),
    PRIVACY_CONSENT_NOT_AGREED(HttpStatus.BAD_REQUEST, "ONBOARDING4002", "개인정보 수집 및 이용은 필수 동의 항목입니다."),
    AGREEMENT_ALREADY_EXISTS(HttpStatus.CONFLICT, "ONBOARDING4091", "이미 약관에 동의한 사용자입니다."),
    NICKNAME_ALREADY_EXISTS(HttpStatus.CONFLICT, "ONBOARDING4092", "이미 사용 중인 닉네임입니다."),
    ;

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;

    @Override
    public ErrorReasonDto getReason() {
        return ErrorReasonDto.builder()
                .isSuccess(false)
                .code(code)
                .message(message)
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
