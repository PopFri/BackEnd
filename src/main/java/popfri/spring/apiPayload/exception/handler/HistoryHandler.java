package popfri.spring.apiPayload.exception.handler;

import popfri.spring.apiPayload.code.BaseErrorCode;
import popfri.spring.apiPayload.exception.GeneralException;

public class HistoryHandler extends GeneralException {

    public HistoryHandler(BaseErrorCode errorCode) {
        super(errorCode);
    }
}