package umc.teumteum.server.domain.teum.exception.status;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;
import umc.teumteum.server.global.apiPayload.code.BaseCode;
import umc.teumteum.server.global.apiPayload.code.ReasonDto;

@Getter
@AllArgsConstructor
public enum TeumSuccessStatus implements BaseCode {

    _TEUM_REQUEST_CREATED(HttpStatus.OK, "TEUM2000", "틈 요청이 성공적으로 생성되었습니다."),
    _TEUM_RESEND_CREATED(HttpStatus.OK, "TEUM2001", "재요청이 성공적으로 생성되었습니다."),
    _TEUM_RECEIVED_LIST_LOADED(HttpStatus.OK, "TEUM2002", "받은 틈 요청 목록이 조회되었습니다."),
    _TEUM_DETAIL_LOADED(HttpStatus.OK, "TEUM2003", "틈 요청 상세 정보가 조회되었습니다."),
    _TEUM_STATUS_UPDATED(HttpStatus.OK, "TEUM2004", "틈 응답 상태가 성공적으로 변경되었습니다."),
    _TEUM_CALENDAR_LOADED(HttpStatus.OK, "TEUM2005", "요청 달력 정보가 조회되었습니다."),
    _TEUM_LIST_BY_DATE_LOADED(HttpStatus.OK, "TEUM2006", "지정한 날짜의 틈 요청 목록이 조회되었습니다."),
    _SCHEDULED_CALENDAR_LOADED(HttpStatus.OK, "TEUM2007", "약속된 틈 달력 정보가 조회되었습니다."),
    _SCHEDULED_LIST_LOADED(HttpStatus.OK, "TEUM2008", "지정한 날짜의 약속된 틈 목록이 조회되었습니다."),
    _SCHEDULED_DETAIL_LOADED(HttpStatus.OK, "TEUM2009", "약속된 틈 상세 정보가 조회되었습니다."),
    _SCHEDULED_EXITED(HttpStatus.OK, "TEUM2010", "약속된 틈에서 성공적으로 나갔습니다."),
    _AVAILABLE_TIME_LOADED(HttpStatus.OK, "TEUM2011", "공통 가능한 시간대가 조회되었습니다."),
    _SHARED_TIME_LOADED(HttpStatus.OK, "TEUM2012", "함께한 틈 시간 정보가 조회되었습니다."),
    _SHARED_LIST_LOADED(HttpStatus.OK, "TEUM2013", "함께한 틈 목록이 조회되었습니다.");

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
