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
    INVALID_DURATION_MINUTES("CLASS_TEMPLATE-003", "진행 시간은 1분 이상 24시간 이하여야 합니다.", HttpStatus.BAD_REQUEST),
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
    INVALID_CLASS_SESSION_DURATION_MINUTES("CLASS_SESSION-003", "진행 시간은 1분 이상 24시간 이하여야 합니다.", HttpStatus.BAD_REQUEST),
    INVALID_CLASS_SESSION_START_AT("CLASS_SESSION-004", "올바른 수업 시작 일시가 필요합니다.", HttpStatus.BAD_REQUEST),
    INVALID_CLASS_SESSION_CAPACITY("CLASS_SESSION-005", "정원은 1명 이상이어야 합니다.", HttpStatus.BAD_REQUEST),
    CLASS_SESSION_STUDIO_REQUIRED("CLASS_SESSION-006", "수업 시설은 필수입니다.", HttpStatus.BAD_REQUEST),
    CLASS_SESSION_INSTRUCTOR_REQUIRED("CLASS_SESSION-007", "담당 강사는 필수입니다.", HttpStatus.BAD_REQUEST),
    CLASS_SESSION_STATUS_REQUIRED("CLASS_SESSION-008", "수업 상태는 필수입니다.", HttpStatus.BAD_REQUEST),
    INVALID_CLASS_SESSION_RECURRENCE("CLASS_SESSION-009", "반복 여부에 맞는 수업 일정 정보가 필요합니다.", HttpStatus.BAD_REQUEST),
    INVALID_CLASS_SESSION_REPEAT_PERIOD("CLASS_SESSION-010", "올바른 반복 기간이 필요합니다.", HttpStatus.BAD_REQUEST),
    INVALID_CLASS_SESSION_RECURRING_DAYS("CLASS_SESSION-011", "반복 요일을 하나 이상 중복 없이 선택해야 합니다.", HttpStatus.BAD_REQUEST),
    CLASS_SESSION_DATES_EMPTY("CLASS_SESSION-012", "생성할 수업 날짜가 없습니다.", HttpStatus.BAD_REQUEST),
    INVALID_CLASS_SESSION_CLASS_TYPE_ID("CLASS_SESSION-013", "올바른 수업 종류가 필요합니다.", HttpStatus.BAD_REQUEST),
    CLASS_SESSION_NOT_FOUND("CLASS_SESSION-014", "수업 회차를 찾을 수 없습니다.", HttpStatus.NOT_FOUND),
    CLASS_SESSION_TIME_CONFLICT("CLASS_SESSION-015", "담당 강사의 기존 수업과 시간이 겹칩니다.", HttpStatus.CONFLICT),
    CLASS_SESSION_CANCELED("CLASS_SESSION-016", "취소된 수업은 수정할 수 없습니다.", HttpStatus.CONFLICT),
    CLASS_SESSION_INSTRUCTOR_NOT_FOUND(
            "CLASS_SESSION-017",
            "담당 가능한 강사 소속을 찾을 수 없습니다.",
            HttpStatus.NOT_FOUND
    ),

    // RESERVATION
    INVALID_RESERVATION_TRANSITION(
            "RESERVATION-001",
            "현재 예약 상태에서는 요청한 상태 전이를 수행할 수 없습니다.",
            HttpStatus.CONFLICT
    ),
    RESERVATION_CANCEL_OCCURRED_AT_REQUIRED(
            "RESERVATION-002",
            "예약 취소 시각은 필수입니다.",
            HttpStatus.BAD_REQUEST
    ),
    RESERVATION_ATTENDANCE_OCCURRED_AT_REQUIRED(
            "RESERVATION-003",
            "예약 출석 처리 시각은 필수입니다.",
            HttpStatus.BAD_REQUEST
    ),
    RESERVATION_ABSENCE_OCCURRED_AT_REQUIRED(
            "RESERVATION-004",
            "예약 결석 처리 시각은 필수입니다.",
            HttpStatus.BAD_REQUEST
    ),

    // WAITING
    INVALID_WAITING_TRANSITION(
            "WAITING-001",
            "현재 대기 상태에서는 요청한 상태 전이를 수행할 수 없습니다.",
            HttpStatus.CONFLICT
    ),
    WAITING_OFFERED_AT_REQUIRED(
            "WAITING-002",
            "대기 제안 시각은 필수입니다.",
            HttpStatus.BAD_REQUEST
    ),
    WAITING_OFFER_EXPIRES_AT_REQUIRED(
            "WAITING-003",
            "대기 제안 만료 시각은 필수입니다.",
            HttpStatus.BAD_REQUEST
    ),
    INVALID_WAITING_OFFER_DEADLINE(
            "WAITING-004",
            "대기 제안 만료 시각은 제안 시각 이후여야 합니다.",
            HttpStatus.BAD_REQUEST
    ),
    WAITING_CANCEL_OCCURRED_AT_REQUIRED(
            "WAITING-005",
            "대기 취소 시각은 필수입니다.",
            HttpStatus.BAD_REQUEST
    ),
    WAITING_EXPIRATION_OCCURRED_AT_REQUIRED(
            "WAITING-006",
            "대기 만료 시각은 필수입니다.",
            HttpStatus.BAD_REQUEST
    ),
    WAITING_ACCEPTANCE_OCCURRED_AT_REQUIRED(
            "WAITING-007",
            "대기 제안 수락 시각은 필수입니다.",
            HttpStatus.BAD_REQUEST
    ),
    WAITING_OFFER_EXPIRED(
            "WAITING-008",
            "대기 제안이 만료되어 수락할 수 없습니다.",
            HttpStatus.CONFLICT
    );

    private final String code;
    private final String message;
    private final HttpStatus status;

    ClassErrorCode(String code, String message, HttpStatus status) {
        this.code = code;
        this.message = message;
        this.status = status;
    }
}
