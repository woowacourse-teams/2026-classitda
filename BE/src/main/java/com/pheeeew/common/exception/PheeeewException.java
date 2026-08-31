package com.pheeeew.common.exception;

import java.util.Objects;
import lombok.Getter;

@Getter
public class PheeeewException extends RuntimeException {

    private final ErrorCode errorCode;

    public PheeeewException(ErrorCode errorCode, Throwable cause) {
        super(Objects.requireNonNull(errorCode).getMessage(), cause);
        this.errorCode = errorCode;
    }
}
