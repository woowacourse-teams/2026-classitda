package com.classitda.classes.exception;

import com.classitda.common.exception.ErrorCode;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ClassErrorCode implements ErrorCode {
    INVALID_NAME("CLASS_TYPE-001", "수업 종류 이름은 1자 이상 50자 이하여야 합니다.", HttpStatus.BAD_REQUEST),
    CLASS_TYPE_NAME_DUPLICATED("CLASS_TYPE-002", "이미 존재하는 수업 종류 이름입니다.", HttpStatus.CONFLICT),
    CLASS_TYPE_NOT_FOUND("CLASS_TYPE-003", "수업 종류를 찾을 수 없습니다.", HttpStatus.NOT_FOUND);

    private final String code;
    private final String message;
    private final HttpStatus status;

    ClassErrorCode(String code, String message, HttpStatus status) {
        this.code = code;
        this.message = message;
        this.status = status;
    }
}
