package com.pheeeew.sigh.exception;

import com.pheeeew.common.exception.PheeeewException;

public class SighException extends PheeeewException {

    public SighException(SighErrorCode errorCode) {
        super(errorCode, null);
    }

    public SighException(SighErrorCode errorCode, Throwable cause) {
        super(errorCode, cause);
    }
}
