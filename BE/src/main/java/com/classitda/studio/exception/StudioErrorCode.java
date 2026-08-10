package com.classitda.studio.exception;

import com.classitda.common.exception.ErrorCode;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum StudioErrorCode implements ErrorCode {
    INVALID_OPERATING_TIME("STUDIO-001", "운영 종료 시간은 시작 시간보다 늦어야 합니다.", HttpStatus.BAD_REQUEST),
    NOT_FOUND("STUDIO-002", "시설을 찾을 수 없습니다.", HttpStatus.NOT_FOUND),
    NOT_OWNER("STUDIO-003", "해당 시설의 대표 강사가 아닙니다.", HttpStatus.FORBIDDEN);

    private final String code;
    private final String message;
    private final HttpStatus status;

    StudioErrorCode(String code, String message, HttpStatus status) {
        this.code = code;
        this.message = message;
        this.status = status;
    }
}
