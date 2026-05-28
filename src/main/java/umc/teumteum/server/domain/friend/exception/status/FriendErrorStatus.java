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
    INVALID_SELF_REQUEST(HttpStatus.BAD_REQUEST, "FRIEND4002", "자기 자신에 대한 요청은 처리할 수 없습니다."),
    NOT_FOLLOWING(HttpStatus.BAD_REQUEST, "FRIEND4003", "팔로우하지 않은 유저입니다."),
    ALREADY_BLOCKED(HttpStatus.BAD_REQUEST, "FRIEND4005", "이미 차단된 사용자입니다."),
    NOT_BLOCKED(HttpStatus.BAD_REQUEST, "FRIEND4006", "차단하지 않은 사용자입니다."),
    BLOCK_ACTION_FORBIDDEN(HttpStatus.BAD_REQUEST, "FRIEND4007", "차단 관계로 인해 요청을 수행할 수 없습니다.")

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
