package umc.teumteum.server.domain.home.exception.status;

import lombok.AllArgsConstructor;
import lombok.Getter;
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
    _TODAY_SCHEDULE(HttpStatus.OK, "HOME20010", "오늘의 스케줄가 성공적으로 조회되었습니다.")
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
