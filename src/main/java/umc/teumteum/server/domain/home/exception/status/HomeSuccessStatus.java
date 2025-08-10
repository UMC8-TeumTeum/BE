package umc.teumteum.server.domain.home.exception.status;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.boot.autoconfigure.graphql.GraphQlProperties;
import org.springframework.http.HttpStatus;
import umc.teumteum.server.global.apiPayload.code.BaseCode;
import umc.teumteum.server.global.apiPayload.code.ReasonDto;

@Getter
@AllArgsConstructor
public enum HomeSuccessStatus implements BaseCode {

    _TODO_CREATED(HttpStatus.OK, "HOME2001", "투두가 성공적으로 생성되었습니다."),
    _TODO_UPDATED(HttpStatus.OK, "HOME2002", "투두 정보가 성공적으로 수정되었습니다."),
    _TODO_LOADED(HttpStatus.OK, "HOME2003", "투두 정보가 성공적으로 조회되었습니다."),
    _TODO_DELETED(HttpStatus.OK, "HOME2004", "투두가 성공적으로 삭제되었습니다."),
    _WISH_CREATED(HttpStatus.OK, "HOME2005","위시가 성공적으로 생성되었습니다."),
    _WISH_LOADED(HttpStatus.OK, "HOME2006","위시 정보가 성공적으로 조회되었습니다."),
    _WISH_DELETED(HttpStatus.OK, "HOME2007","위시가 성공적으로 삭제되었습니다."),
    _WISH_UPDATED(HttpStatus.OK, "HOME2008", "위시 정보가 성공적으로 수정되었습니다."),
    _WISHLIST_LOADED(HttpStatus.OK, "HOME2009", "위시리스트가 성공적으로 조회되었습니다."),
    _TODAY_SCHEDULE(HttpStatus.OK, "HOME20010", "오늘의 스케줄이 성공적으로 조회되었습니다."),
    _WISH_ASSIGNED(HttpStatus.OK, "HOME20011","선택된 위시가 투두로 등록되었습니다."),
    _CATEGORY_LOADED(HttpStatus.OK, "HOME20012","카테고리 정보가 조회되었습니다."),
    _TEUMTIME_LOADED(HttpStatus.OK, "HOME20013", "지금까지 채운 빈틈 시간이 성공적으로 조회되었습니다."),
    _CALENDAR_LOADED(HttpStatus.OK,"HOME20014","캘린더 정보가 성공적으로 조회되었습니다."),
    _TODOLIST_LOADED(HttpStatus.OK,"HOME20015","투두리스트가 성공적으로 조회되었습니다."),
    _REMINDER_LOADED(HttpStatus.OK, "HOME20016", "리마인드 알림 정보가 성공적으로 조회되었습니다."),
    _ALARM_UPDATED(HttpStatus.OK,"HOME20017","리마인드 알림 정보가 성공적으로 변경되었습니다."),
    _ACTIVITY_LOADED(HttpStatus.OK, "ACTIVITY2001", "채움활동 위시가 성공적으로 조회되었습니다."),
    _AI_ACTIVITY_LOADED(HttpStatus.OK, "ACTIVITY2002", "채움활동 ai컨텐츠가 성공적으로 조회되었습니다.")
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
