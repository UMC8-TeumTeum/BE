package umc.teumteum.server.domain.report.exception;

import umc.teumteum.server.global.apiPayload.code.BaseErrorCode;
import umc.teumteum.server.global.exception.GeneralException;

public class ReportException extends GeneralException {
    public ReportException(BaseErrorCode errorCode) {
        super(errorCode);
    }
}