package com.classitda.classes.exception;

import com.classitda.common.exception.ClassitdaException;

public class ClassTypeException extends ClassitdaException {

    public ClassTypeException(ClassTypeErrorCode errorCode) {
        super(errorCode);
    }
}
