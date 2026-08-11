package com.classitda.studio.exception;

import com.classitda.common.exception.ErrorCode;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum StudioErrorCode implements ErrorCode {
    // STUDIO
    INVALID_OPERATING_TIME("STUDIO-001", "운영 종료 시간은 시작 시간보다 늦어야 합니다.", HttpStatus.BAD_REQUEST),
    NOT_FOUND("STUDIO-002", "시설을 찾을 수 없습니다.", HttpStatus.NOT_FOUND),
    NOT_OWNER("STUDIO-003", "해당 시설의 대표 강사가 아닙니다.", HttpStatus.FORBIDDEN),

    // ROOM
    ROOM_NOT_FOUND("ROOM-001", "룸을 찾을 수 없습니다.", HttpStatus.NOT_FOUND),
    ROOM_NAME_DUPLICATED("ROOM-002", "이미 사용 중인 룸 이름입니다.", HttpStatus.CONFLICT),

    // POLICY
    POLICY_NOT_FOUND("POLICY-001", "운영 정책을 찾을 수 없습니다.", HttpStatus.NOT_FOUND),
    POLICY_ALREADY_EXISTS("POLICY-002", "이미 운영 정책이 등록된 시설입니다.", HttpStatus.CONFLICT);

    private final String code;
    private final String message;
    private final HttpStatus status;

    StudioErrorCode(String code, String message, HttpStatus status) {
        this.code = code;
        this.message = message;
        this.status = status;
    }
}
