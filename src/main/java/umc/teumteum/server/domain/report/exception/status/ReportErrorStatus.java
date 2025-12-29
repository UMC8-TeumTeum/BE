package umc.teumteum.server.domain.report.exception.status;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;
import umc.teumteum.server.global.apiPayload.code.BaseErrorCode;
import umc.teumteum.server.global.apiPayload.code.ErrorReasonDto;

@Getter
@AllArgsConstructor
public enum ReportErrorStatus implements BaseErrorCode {

    REPORT_REASON_NOT_FOUND(HttpStatus.NOT_FOUND, "REPORT4001", "존재하지 않는 신고 사유입니다."),
    REPORT_TARGET_NOT_FOUND(HttpStatus.NOT_FOUND, "REPORT4002", "신고 대상이 존재하지 않습니다."),
    REPORT_SELF_NOT_ALLOWED(HttpStatus.BAD_REQUEST, "REPORT4003", "자기 자신은 신고할 수 없습니다."),
    REPORT_OTHER_REASON_REQUIRED(HttpStatus.BAD_REQUEST, "REPORT4004", "기타 사유 선택 시, 구체적인 내용을 입력해야 합니다."),
    REPORT_INVALID_TARGET_TYPE(HttpStatus.BAD_REQUEST, "REPORT4005", "지원하지 않는 신고 대상입니다."),
    REPORT_INVALID_STATUS(HttpStatus.BAD_REQUEST, "REPORT4006", "대기 중인 요청만 신고할 수 있습니다."),

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