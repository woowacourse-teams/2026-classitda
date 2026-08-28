package com.classitda.classes.exception;

import com.classitda.common.exception.ClassitdaException;

public class ClassException extends ClassitdaException {

    public ClassException(ClassErrorCode errorCode) {
        super(errorCode);
    }
}
