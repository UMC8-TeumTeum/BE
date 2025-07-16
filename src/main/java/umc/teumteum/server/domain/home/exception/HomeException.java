package umc.teumteum.server.domain.home.exception;

import umc.teumteum.server.global.apiPayload.code.BaseErrorCode;
import umc.teumteum.server.global.exception.GeneralException;

public class HomeException extends GeneralException {
    public HomeException(BaseErrorCode errorCode){
        super(errorCode);
    }
}
