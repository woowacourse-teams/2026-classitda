package com.classitda.authentication.exception;

import com.classitda.common.exception.ClassitdaException;

public class AuthException extends ClassitdaException {

    public AuthException(AuthErrorCode errorCode) {
        super(errorCode);
    }
}
