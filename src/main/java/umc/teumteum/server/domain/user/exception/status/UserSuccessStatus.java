package umc.teumteum.server.domain.user.exception.status;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;
import umc.teumteum.server.global.apiPayload.code.BaseCode;
import umc.teumteum.server.global.apiPayload.code.ReasonDto;

@Getter
@AllArgsConstructor
public enum UserSuccessStatus implements BaseCode {

    _USER_FOUND(HttpStatus.OK, "USER2001", "사용자 조회 성공"),


    // 사용자 온보딩
    AGREEMENT_SAVED(HttpStatus.OK, "ONBOARDING2001", "약관 동의가 완료되었습니다."),
    NICKNAME_JOB_SAVED(HttpStatus.OK, "ONBOARDING2002", "닉네임과 분야/직종 등록이 완료되었습니다."),
    SLEEP_PATTERN_SAVED(HttpStatus.OK, "ONBOARDING2003", "수면 패턴 등록이 완료되었습니다."),

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
