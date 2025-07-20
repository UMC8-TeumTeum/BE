package umc.teumteum.server.domain.teum.exception.status;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;
import umc.teumteum.server.global.apiPayload.code.BaseErrorCode;
import umc.teumteum.server.global.apiPayload.code.ErrorReasonDto;

@Getter
@AllArgsConstructor
public enum TeumErrorStatus implements BaseErrorCode {

    TEUM_REQUEST_NOT_FOUND(HttpStatus.NOT_FOUND, "TEUM4040", "존재하지 않는 틈 요청입니다."),
    TEUM_RESPONSE_NOT_FOUND(HttpStatus.NOT_FOUND, "TEUM4041", "존재하지 않는 틈 응답입니다."),
    USER_NOT_ELIGIBLE(HttpStatus.FORBIDDEN, "TEUM4030", "요청 또는 응답에 대한 권한이 없습니다."),
    INVALID_TEUM_TIME(HttpStatus.BAD_REQUEST, "TEUM4001", "시작 시간과 종료 시간이 유효하지 않습니다."),
    DUPLICATE_RECEIVER(HttpStatus.CONFLICT, "TEUM4090", "수신자 목록에 중복된 사용자가 포함되어 있습니다."),
    CANNOT_REQUEST_SELF(HttpStatus.CONFLICT, "TEUM4091", "자기 자신에게 틈 요청을 보낼 수 없습니다."),
    SCHEDULE_CONFLICT(HttpStatus.CONFLICT, "TEUM4092", "이미 해당 시간에 다른 스케줄이 존재합니다."),
    REQUEST_ALREADY_CLOSED(HttpStatus.BAD_REQUEST, "TEUM4002", "이미 마감된 요청입니다."),
    INVALID_PARENT_REQUEST(HttpStatus.BAD_REQUEST, "TEUM4003", "재요청의 기준이 되는 요청이 올바르지 않습니다."),
    REQUEST_NOT_ONE_TO_ONE(HttpStatus.BAD_REQUEST, "TEUM4004", "재요청은 수신자가 1명인 요청에 대해서만 가능합니다."),
    REQUEST_ALREADY_RESENT(HttpStatus.BAD_REQUEST, "TEUM4005", "재요청을 기반으로 다시 재요청할 수 없습니다."),
    INVALID_RESPONSE_STATUS(HttpStatus.BAD_REQUEST, "TEUM4006", "응답 status 값은 accepted 또는 rejected 이어야 합니다.");

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
