package popfri.spring.apiPayload.exception.handler;

import popfri.spring.apiPayload.code.BaseErrorCode;
import popfri.spring.apiPayload.exception.GeneralException;

public class ReviewHandler extends GeneralException {

    public ReviewHandler(BaseErrorCode errorCode) {
        super(errorCode);
    }
}
