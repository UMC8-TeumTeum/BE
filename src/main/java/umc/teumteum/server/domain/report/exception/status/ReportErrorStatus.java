package umc.teumteum.server.domain.report.exception.status;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;
import umc.teumteum.server.global.apiPayload.code.BaseErrorCode;
import umc.teumteum.server.global.apiPayload.code.ErrorReasonDto;

@Getter
@AllArgsConstructor
public enum ReportErrorStatus implements BaseErrorCode {

    // 조회 관련 (404 Not Found)
    REPORT_REASON_NOT_FOUND(HttpStatus.NOT_FOUND, "REPORT4041", "존재하지 않는 신고 사유입니다."),
    REPORT_TARGET_NOT_FOUND(HttpStatus.NOT_FOUND, "REPORT4042", "신고 대상이 존재하지 않습니다."),
    REPORT_NOT_FOUND(HttpStatus.NOT_FOUND, "REPORT4043", "신고 내역을 찾을 수 없습니다."), // 신규 추가

    // 제한 및 검증 관련 (400 Bad Request)
    REPORT_SELF_NOT_ALLOWED(HttpStatus.BAD_REQUEST, "REPORT4001", "자기 자신은 신고할 수 없습니다."),
    REPORT_OTHER_REASON_REQUIRED(HttpStatus.BAD_REQUEST, "REPORT4002", "기타 사유 선택 시, 구체적인 내용을 입력해야 합니다."),
    REPORT_INVALID_TARGET_TYPE(HttpStatus.BAD_REQUEST, "REPORT4003", "지원하지 않는 신고 대상입니다."),
    REPORT_INVALID_STATUS(HttpStatus.BAD_REQUEST, "REPORT4004", "대기 중인 요청만 신고할 수 있습니다."),
    REPORT_ALREADY_EXISTS(HttpStatus.BAD_REQUEST, "REPORT4005", "이미 신고한 대상입니다."), // 중복 신고 방지용 추가
    REPORT_ALREADY_RESOLVED(HttpStatus.BAD_REQUEST, "REPORT4006", "이미 처리가 완료된 신고 내역입니다.")

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