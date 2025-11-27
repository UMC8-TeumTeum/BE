package umc.teumteum.server.domain.friend.exception.status;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;
import umc.teumteum.server.global.apiPayload.code.BaseCode;
import umc.teumteum.server.global.apiPayload.code.ReasonDto;

@Getter
@AllArgsConstructor
public enum FriendSuccessStatus implements BaseCode {
    _FOLLOW_SUCCESS(HttpStatus.OK, "FRIEND2000", "팔로우가 성공적으로 완료되었습니다."),
    _UNFOLLOW_SUCCESS(HttpStatus.OK, "FRIEND2001", "언팔로우가 성공적으로 완료되었습니다."),
    _GET_FRIENDS_SUCCESS(HttpStatus.OK, "FRIEND2002", "친구 목록 조회에 성공하였습니다."),
    _FAVORITE_UPDATE_SUCCESS(HttpStatus.OK, "FRIEND2003", "즐겨찾기 설정/해제가 완료되었습니다."),
    _GET_FRIEND_TEUM_TIME_SUCCESS(HttpStatus.OK, "FRIEND2004", "친구 빈틈 시간 조회에 성공하였습니다."),
    _GET_FRIEND_PUBLIC_TODO_SUCCESS(HttpStatus.OK, "FRIEND2005", "친구의 공개 투두 조회에 성공하였습니다."),
    _BLOCK_SUCCESS(HttpStatus.OK, "FRIEND2006", "유저 차단에 성공하였습니다."),
    _UNBLOCK_SUCCESS(HttpStatus.OK, "FRIEND2007", "유저 차단 해제에 성공하였습니다."),

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
