package com.classitda.classes.exception;

import com.classitda.common.exception.ErrorCode;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ClassErrorCode implements ErrorCode {

    // CLASS TYPE
    INVALID_NAME("CLASS_TYPE-001", "수업 종류 이름은 1자 이상 50자 이하여야 합니다.", HttpStatus.BAD_REQUEST),
    CLASS_TYPE_NAME_DUPLICATED("CLASS_TYPE-002", "이미 존재하는 수업 종류 이름입니다.", HttpStatus.CONFLICT),
    CLASS_TYPE_NOT_FOUND("CLASS_TYPE-003", "수업 종류를 찾을 수 없습니다.", HttpStatus.NOT_FOUND),
    CLASS_TYPE_IN_USE("CLASS_TYPE-004", "사용 중인 수업 종류는 삭제할 수 없습니다.", HttpStatus.CONFLICT),

    // CLASS TEMPLATE
    INVALID_CLASS_TEMPLATE_NAME("CLASS_TEMPLATE-001", "수업 템플릿 이름은 1자 이상 100자 이하여야 합니다.", HttpStatus.BAD_REQUEST),
    INVALID_CLASS_FORM("CLASS_TEMPLATE-002", "수업 형태는 필수입니다.", HttpStatus.BAD_REQUEST),
    INVALID_DURATION_MINUTES("CLASS_TEMPLATE-003", "진행 시간은 1분 이상이어야 합니다.", HttpStatus.BAD_REQUEST),
    INVALID_CAPACITY("CLASS_TEMPLATE-004", "정원은 1명 이상이어야 합니다.", HttpStatus.BAD_REQUEST),
    CLASS_TYPES_REQUIRED("CLASS_TEMPLATE-005", "수업 종류를 하나 이상 선택해야 합니다.", HttpStatus.BAD_REQUEST),
    CLASS_TYPES_DUPLICATED("CLASS_TEMPLATE-006", "중복된 수업 종류를 선택할 수 없습니다.", HttpStatus.BAD_REQUEST),
    CLASS_TEMPLATE_NOT_FOUND("CLASS_TEMPLATE-007", "수업 템플릿을 찾을 수 없습니다.", HttpStatus.NOT_FOUND),
    INVALID_START_TIME("CLASS_TEMPLATE-009", "시작 시간은 필수입니다.", HttpStatus.BAD_REQUEST),
    INVALID_RECURRING_DAY("CLASS_TEMPLATE-011", "반복 요일에는 null을 포함할 수 없습니다.", HttpStatus.BAD_REQUEST),
    CLASS_TEMPLATE_STUDIO_REQUIRED("CLASS_TEMPLATE-012", "수업 템플릿 시설은 필수입니다.", HttpStatus.BAD_REQUEST),

    // CLASS SESSION
    INVALID_CLASS_SESSION_NAME("CLASS_SESSION-001", "수업 이름은 1자 이상 100자 이하여야 합니다.", HttpStatus.BAD_REQUEST),
    INVALID_CLASS_SESSION_FORM("CLASS_SESSION-002", "수업 형태는 필수입니다.", HttpStatus.BAD_REQUEST),
    INVALID_CLASS_SESSION_DURATION_MINUTES("CLASS_SESSION-003", "진행 시간은 1분 이상이어야 합니다.", HttpStatus.BAD_REQUEST),
    INVALID_CLASS_SESSION_START_AT("CLASS_SESSION-004", "올바른 수업 시작 일시가 필요합니다.", HttpStatus.BAD_REQUEST),
    INVALID_CLASS_SESSION_CAPACITY("CLASS_SESSION-005", "정원은 1명 이상이어야 합니다.", HttpStatus.BAD_REQUEST),
    CLASS_SESSION_STUDIO_REQUIRED("CLASS_SESSION-006", "수업 시설은 필수입니다.", HttpStatus.BAD_REQUEST),
    CLASS_SESSION_INSTRUCTOR_REQUIRED("CLASS_SESSION-007", "담당 강사는 필수입니다.", HttpStatus.BAD_REQUEST),
    CLASS_SESSION_STATUS_REQUIRED("CLASS_SESSION-008", "수업 상태는 필수입니다.", HttpStatus.BAD_REQUEST);

    private final String code;
    private final String message;
    private final HttpStatus status;

    ClassErrorCode(String code, String message, HttpStatus status) {
        this.code = code;
        this.message = message;
        this.status = status;
    }
}
