package umc.teumteum.server.domain.home.exception.status;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;
import umc.teumteum.server.global.apiPayload.code.BaseErrorCode;
import umc.teumteum.server.global.apiPayload.code.ErrorReasonDto;

@Getter
@AllArgsConstructor
public enum HomeErrorStatus implements BaseErrorCode {
    _INVALID_TIME_RANGE(HttpStatus.BAD_REQUEST, "HOME4001", "endTime은 startTime을 앞설 수 없습니다."),
    _INVALID_DURATION(HttpStatus.BAD_REQUEST,"HOME4002","잘못된 조회 기간입니다."),
    _CANNOT_UPDATE_ROUTINE(HttpStatus.BAD_REQUEST,"HOME4003","반복일정은 수정할 수 없습니다."),
    _INVALID_VIRTUAL_ID(HttpStatus.BAD_REQUEST,"HOME4004","잘못된 루틴 ID 형식입니다."),
    _SCHEDULE_NOT_FOUND(HttpStatus.NOT_FOUND, "HOME4041", "해당 정보를 찾을 수 없습니다."),
    _CATEGORY_NOT_FOUND(HttpStatus.NOT_FOUND,"HOME4042","해당 카테고리를 찾을 수 없습니다."),
    _CATEGORY_REQUIRED(HttpStatus.BAD_REQUEST, "HOME4003", "카테고리는 필수 항목입니다."),
    _CATEGORY_INPUT_CONFLICT(HttpStatus.BAD_REQUEST, "HOME4004", "카테고리와 직접 입력은 둘 중 하나만 선택해야 합니다."),
    _WISH_NOT_FOUND(HttpStatus.NOT_FOUND,"HOME4043","해당 위시 정보를 찾을 수 없습니다."),
    _ROUTINE_NOT_FOUND(HttpStatus.NOT_FOUND,"HOME4044","해당 루틴 정보를 찾을 수 없습니다."),
    _SCHEDULE_CONFLICT(HttpStatus.CONFLICT, "HOME4092","해당 시간에 스케줄이 존재합니다.")
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
