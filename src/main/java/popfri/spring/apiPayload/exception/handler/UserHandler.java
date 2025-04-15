package popfri.spring.apiPayload.exception.handler;

import popfri.spring.apiPayload.code.BaseErrorCode;
import popfri.spring.apiPayload.exception.GeneralException;

public class UserHandler extends GeneralException {

    public UserHandler(BaseErrorCode errorCode) {
        super(errorCode);
    }
}