package com.classitda.studio.exception;

import com.classitda.common.exception.ErrorCode;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum StudioErrorCode implements ErrorCode {
    // STUDIO
    INVALID_OPERATING_TIME("STUDIO-001", "운영 종료 시간은 시작 시간보다 늦어야 합니다.", HttpStatus.BAD_REQUEST),
    NOT_FOUND("STUDIO-002", "시설을 찾을 수 없습니다.", HttpStatus.NOT_FOUND),
    MEMBER_NOT_FOUND("STUDIO-003", "회원을 찾을 수 없습니다.", HttpStatus.NOT_FOUND),

    // ROOM
    ROOM_NOT_FOUND("ROOM-001", "룸을 찾을 수 없습니다.", HttpStatus.NOT_FOUND),
    ROOM_NAME_DUPLICATED("ROOM-002", "이미 사용 중인 룸 이름입니다.", HttpStatus.CONFLICT),

    // POLICY
    POLICY_NOT_FOUND("POLICY-001", "운영 정책을 찾을 수 없습니다.", HttpStatus.NOT_FOUND),
    POLICY_ALREADY_EXISTS("POLICY-002", "이미 운영 정책이 등록된 시설입니다.", HttpStatus.CONFLICT),

    // MEMBERSHIP
    NOT_MEMBERSHIP("MEMBERSHIP-001", "해당 시설의 소속이 아닙니다.", HttpStatus.FORBIDDEN),
    MEMBERSHIP_INACTIVE("MEMBERSHIP-002", "이용이 정지된 소속입니다.", HttpStatus.FORBIDDEN),
    INVALID_MEMBERSHIP_NAME("MEMBERSHIP-003", "소속 이름은 1자 이상 50자 이하여야 합니다.", HttpStatus.BAD_REQUEST),
    MEMBERSHIP_ALREADY_EXISTS("MEMBERSHIP-004", "이미 시설에 등록된 회원입니다.", HttpStatus.CONFLICT),
    MEMBERSHIP_NOT_FOUND("MEMBERSHIP-005", "시설 소속을 찾을 수 없습니다.", HttpStatus.NOT_FOUND),

    STUDIO_ROLE_NOT_FOUND("ROLE-001", "시설 역할을 찾을 수 없습니다.", HttpStatus.NOT_FOUND),

    // PERMISSION
    PERMISSION_DENIED("PERMISSION-001", "이 작업을 수행할 권한이 없습니다.", HttpStatus.FORBIDDEN);

    private final String code;
    private final String message;
    private final HttpStatus status;

    StudioErrorCode(String code, String message, HttpStatus status) {
        this.code = code;
        this.message = message;
        this.status = status;
    }
}
