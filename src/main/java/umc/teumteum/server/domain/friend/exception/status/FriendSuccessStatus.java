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
    _GET_FRIENDS_SUCCESS(HttpStatus.OK, "FRIEND2002", "친구 목록 조회에 성공하였습니다.");

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
