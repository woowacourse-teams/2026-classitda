package com.classitda.studio.exception;

import com.classitda.common.exception.ClassitdaException;
import com.classitda.common.exception.ErrorCode;

public class StudioException extends ClassitdaException {

    public StudioException(ErrorCode errorCode) {
        super(errorCode);
    }
}
