package popfri.spring.apiPayload.exception.handler;

import popfri.spring.apiPayload.code.BaseErrorCode;
import popfri.spring.apiPayload.exception.GeneralException;

public class MovieHandler extends GeneralException {

    public MovieHandler(BaseErrorCode errorCode) {
        super(errorCode);
    }
}
