package com.classitda.passproduct.exception;

import com.classitda.common.exception.ClassitdaException;

public class PassProductException extends ClassitdaException {

    public PassProductException(PassProductErrorCode errorCode) {
        super(errorCode);
    }
}
