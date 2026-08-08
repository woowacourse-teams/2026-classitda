package com.classitda.common.exception;

import java.util.Objects;

public record ErrorResponse(String code, String message) {

    public ErrorResponse {
        Objects.requireNonNull(code);
        Objects.requireNonNull(message);
    }

    public static ErrorResponse from(ErrorCode errorCode) {
        Objects.requireNonNull(errorCode);
        return new ErrorResponse(errorCode.getCode(), errorCode.getMessage());
    }
}
