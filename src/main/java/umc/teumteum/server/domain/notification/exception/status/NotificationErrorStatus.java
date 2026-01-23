package umc.teumteum.server.domain.notification.exception.status;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;
import umc.teumteum.server.global.apiPayload.code.BaseErrorCode;
import umc.teumteum.server.global.apiPayload.code.ErrorReasonDto;

@Getter
@AllArgsConstructor
public enum NotificationErrorStatus implements BaseErrorCode {

    NOTIFICATION_NOT_FOUND(HttpStatus.NOT_FOUND, "NOTIFICATION4040", "존재하지 않는 알림입니다."),
    FORBIDDEN_NOTIFICATION(HttpStatus.FORBIDDEN, "NOTIFICATION4030", "해당 알림에 대한 권한이 없습니다.");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;

    @Override
    public ErrorReasonDto getReason() {
        return ErrorReasonDto.builder()
                .code(code)
                .message(message)
                .build();
    }

    @Override
    public ErrorReasonDto getReasonHttpStatus() {
        return ErrorReasonDto.builder()
                .httpStatus(httpStatus)
                .code(code)
                .message(message)
                .build();
    }
}
