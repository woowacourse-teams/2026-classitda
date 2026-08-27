package com.classitda.common.exception;

import java.util.Objects;
import lombok.Getter;

@Getter
public class ClassitdaException extends RuntimeException {

    private final ErrorCode errorCode;

    public ClassitdaException(ErrorCode errorCode) {
        super(Objects.requireNonNull(errorCode).getMessage());
        this.errorCode = errorCode;
    }
}
