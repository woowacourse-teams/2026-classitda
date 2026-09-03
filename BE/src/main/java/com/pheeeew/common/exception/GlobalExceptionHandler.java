package com.pheeeew.common.exception;

import static com.pheeeew.common.exception.CommonErrorCode.ENDPOINT_NOT_FOUND;
import static com.pheeeew.common.exception.CommonErrorCode.INTERNAL_SERVER_ERROR;
import static com.pheeeew.common.exception.CommonErrorCode.INVALID_REQUEST;

import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(PheeeewException.class)
    public ResponseEntity<ErrorResponse> handlePheeeewException(PheeeewException exception) {
        return toResponseEntity(exception.getErrorCode());
    }

    @ExceptionHandler({
            MethodArgumentNotValidException.class,
            BindException.class,
            HandlerMethodValidationException.class,
            MethodArgumentTypeMismatchException.class,
            ConstraintViolationException.class,
            HttpMessageNotReadableException.class
    })
    public ResponseEntity<ErrorResponse> handleInvalidRequest(Exception exception) {
        return toResponseEntity(INVALID_REQUEST);
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ErrorResponse> handleNoResourceFound(NoResourceFoundException exception) {
        return toResponseEntity(ENDPOINT_NOT_FOUND);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleUnexpectedException(Exception exception) {
        log.error(
                "처리되지 않은 예외가 발생했습니다. exceptionType={}",
                exception.getClass().getName(),
                exception
        );
        return toResponseEntity(INTERNAL_SERVER_ERROR);
    }

    private ResponseEntity<ErrorResponse> toResponseEntity(ErrorCode errorCode) {
        return ResponseEntity.status(errorCode.getStatus())
                .body(ErrorResponse.from(errorCode));
    }
}
