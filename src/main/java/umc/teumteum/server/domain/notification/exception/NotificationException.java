package umc.teumteum.server.domain.notification.exception;

import umc.teumteum.server.global.apiPayload.code.BaseErrorCode;
import umc.teumteum.server.global.exception.GeneralException;

public class NotificationException extends GeneralException {
    public NotificationException(BaseErrorCode code) {
        super(code);
    }
}
