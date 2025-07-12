package umc.teumteum.server.domain.friend.exception.status;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;
import umc.teumteum.server.global.apiPayload.code.BaseErrorCode;
import umc.teumteum.server.global.apiPayload.code.ErrorReasonDto;

@Getter
@AllArgsConstructor
public enum FriendErrorStatus implements BaseErrorCode {

    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "FRIEND4040", "존재하지 않는 유저입니다."),
    ALREADY_FOLLOWING(HttpStatus.BAD_REQUEST, "FRIEND4001", "이미 팔로우한 유저입니다."),
    CANNOT_FOLLOW_SELF(HttpStatus.CONFLICT, "FRIEND4090", "자기 자신은 팔로우할 수 없습니다."),
    BLOCKED_USER(HttpStatus.FORBIDDEN, "FRIEND4030", "차단된 유저는 팔로우할 수 없습니다.");

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
