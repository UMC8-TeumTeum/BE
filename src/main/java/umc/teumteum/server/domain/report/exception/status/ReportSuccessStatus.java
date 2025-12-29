package umc.teumteum.server.domain.report.exception.status;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;
import umc.teumteum.server.global.apiPayload.code.BaseCode;
import umc.teumteum.server.global.apiPayload.code.ReasonDto;

@Getter
@AllArgsConstructor
public enum ReportSuccessStatus implements BaseCode {

    REPORT_CREATED(HttpStatus.OK, "REPORT2001", "신고가 성공적으로 접수되었습니다."),
    REPORT_DETAIL_FETCHED(HttpStatus.OK, "REPORT2002", "신고 상세 정보를 성공적으로 조회했습니다.");
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