package com.classitda.common.exception;

import com.classitda.common.response.ErrorResponse;
import jakarta.validation.ConstraintViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindException;
import org.springframework.web.accept.InvalidApiVersionException;
import org.springframework.web.accept.MissingApiVersionException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ClassitdaException.class)
    public ResponseEntity<ErrorResponse> handleCommonException(ClassitdaException exception) {
        return toResponseEntity(exception.getErrorCode());
    }

    @ExceptionHandler({
            MethodArgumentNotValidException.class,
            BindException.class,
            HandlerMethodValidationException.class,
            ConstraintViolationException.class
    })
    public ResponseEntity<ErrorResponse> handleInvalidInput(Exception exception) {
        return toResponseEntity(ErrorCode.INVALID_INPUT);
    }

    @ExceptionHandler(MissingApiVersionException.class)
    public ResponseEntity<ErrorResponse> handleMissingApiVersion(MissingApiVersionException exception) {
        return toResponseEntity(ErrorCode.API_VERSION_REQUIRED);
    }

    @ExceptionHandler(InvalidApiVersionException.class)
    public ResponseEntity<ErrorResponse> handleInvalidApiVersion(InvalidApiVersionException exception) {
        return toResponseEntity(ErrorCode.API_VERSION_UNSUPPORTED);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleUnexpectedException(Exception exception) {
        return toResponseEntity(ErrorCode.INTERNAL_SERVER_ERROR);
    }

    private ResponseEntity<ErrorResponse> toResponseEntity(ErrorCode errorCode) {
        return ResponseEntity.status(errorCode.getStatus())
                .body(ErrorResponse.from(errorCode));
    }
}
