package popfri.spring.apiPayload.exception.handler;

import popfri.spring.apiPayload.code.BaseErrorCode;
import popfri.spring.apiPayload.exception.GeneralException;

public class TempHandler extends GeneralException {

    public TempHandler(BaseErrorCode errorCode) {
        super(errorCode);
    }
}
