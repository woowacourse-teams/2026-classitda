package com.classitda.common.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum CommonErrorCode implements ErrorCode {
    INVALID_INPUT("COMMON-001", "요청 값이 올바르지 않습니다.", HttpStatus.BAD_REQUEST),
    INTERNAL_SERVER_ERROR("COMMON-002", "서버 내부 오류가 발생했습니다.", HttpStatus.INTERNAL_SERVER_ERROR),
    API_VERSION_REQUIRED("API-001", "X-API-Version 헤더는 필수입니다.", HttpStatus.BAD_REQUEST),
    API_VERSION_UNSUPPORTED("API-002", "지원하지 않는 API 버전입니다.", HttpStatus.BAD_REQUEST);

    private final String code;
    private final String message;
    private final HttpStatus status;

    CommonErrorCode(String code, String message, HttpStatus status) {
        this.code = code;
        this.message = message;
        this.status = status;
    }
}
