package umc.teumteum.server.domain.notification.exception.status;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;
import umc.teumteum.server.global.apiPayload.code.BaseCode;
import umc.teumteum.server.global.apiPayload.code.ReasonDto;

@Getter
@AllArgsConstructor
public enum NotificationSuccessStatus implements BaseCode {

    NOTIFICATION_READ_SUCCESS(HttpStatus.OK, "NOTIFICATION2000", "알림 읽음 처리에 성공했습니다."),
    NOTIFICATION_LIST_FETCH_SUCCESS(HttpStatus.OK, "NOTIFICATION2001", "알림 목록 조회에 성공했습니다.");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;

    @Override
    public ReasonDto getReason() {
        return ReasonDto.builder()
                .code(code)
                .message(message)
                .build();
    }

    @Override
    public ReasonDto getReasonHttpStatus() {
        return ReasonDto.builder()
                .httpStatus(httpStatus)
                .code(code)
                .message(message)
                .build();
    }
}
