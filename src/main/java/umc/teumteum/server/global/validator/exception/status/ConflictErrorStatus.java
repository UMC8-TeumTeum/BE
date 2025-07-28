package umc.teumteum.server.global.validator.exception.status;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;
import umc.teumteum.server.global.apiPayload.code.BaseErrorCode;
import umc.teumteum.server.global.apiPayload.code.ErrorReasonDto;

@Getter
@AllArgsConstructor
public enum ConflictErrorStatus implements BaseErrorCode {

    /**
     * 중복 시간 관련 충돌 예외
     */
    SCHEDULE_CONFLICT(HttpStatus.CONFLICT, "CONFLICT4091", "해당 시간에는 일정이 존재합니다."),
    TEUM_REQUEST_CONFLICT(HttpStatus.CONFLICT, "CONFLICT4092", "해당 시간에는 틈 요청이 존재합니다."),
    ROUTINE_CONFLICT(HttpStatus.CONFLICT, "CONFLICT4093", "해당 시간에는 반복 일정이 존재합니다."),
    SLEEP_PATTERN_CONFLICT(HttpStatus.CONFLICT, "CONFLICT4094", "해당 시간에는 수면 패턴이 존재합니다.");

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
