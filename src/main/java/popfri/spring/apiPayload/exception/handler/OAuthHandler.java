package popfri.spring.apiPayload.exception.handler;

import popfri.spring.apiPayload.code.BaseErrorCode;
import popfri.spring.apiPayload.exception.GeneralException;

public class OAuthHandler extends GeneralException {
    public OAuthHandler(BaseErrorCode errorCode) {
        super(errorCode);
    }
}
