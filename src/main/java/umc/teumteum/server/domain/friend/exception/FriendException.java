package umc.teumteum.server.domain.friend.exception;

import umc.teumteum.server.global.apiPayload.code.BaseErrorCode;
import umc.teumteum.server.global.exception.GeneralException;

public class FriendException extends GeneralException {
    public FriendException(BaseErrorCode errorCode){
        super(errorCode);
    }
}
