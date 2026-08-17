package com.classitda.passproduct.exception;

import com.classitda.common.exception.ErrorCode;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum PassProductErrorCode implements ErrorCode {
    INVALID_NAME("PASS_PRODUCT-001", "수강권 이름은 1자 이상 100자 이하여야 합니다.", HttpStatus.BAD_REQUEST),
    INVALID_CLASS_KIND("PASS_PRODUCT-002", "수업 형태는 필수입니다.", HttpStatus.BAD_REQUEST),
    INVALID_VALID_PERIOD("PASS_PRODUCT-003", "유효 기간은 기간과 단위를 함께 지정해야 하며 1 이상이어야 합니다.", HttpStatus.BAD_REQUEST),
    INVALID_TOTAL_COUNT("PASS_PRODUCT-004", "수강 가능 횟수는 1회 이상이어야 합니다.", HttpStatus.BAD_REQUEST),
    NO_EXPIRATION_CONDITION("PASS_PRODUCT-005", "유효 기간과 수강 가능 횟수를 모두 무제한으로 지정할 수 없습니다.", HttpStatus.BAD_REQUEST),
    INVALID_HOLD_DAYS("PASS_PRODUCT-006", "홀딩 가능 일수는 0일 이상이어야 합니다.", HttpStatus.BAD_REQUEST),
    HOLD_DAYS_NOT_ALLOWED("PASS_PRODUCT-007", "유효 기간이 무제한이면 홀딩할 수 없습니다.", HttpStatus.BAD_REQUEST),
    PASS_PRODUCT_NOT_FOUND("PASS_PRODUCT-008", "수강권을 찾을 수 없습니다.", HttpStatus.NOT_FOUND),
    CLASS_TYPE_REQUIRED("PASS_PRODUCT-009", "수업 종류를 하나 이상 지정해야 합니다.", HttpStatus.BAD_REQUEST),
    MEMBER_PASS_PRODUCT_NOT_FOUND("PASS_PRODUCT-010", "보유 수강권을 찾을 수 없습니다.", HttpStatus.NOT_FOUND),
    MEMBER_PASS_PRODUCT_UNAVAILABLE(
            "PASS_PRODUCT-011",
            "현재 사용할 수 없는 수강권입니다.",
            HttpStatus.CONFLICT
    );

    private final String code;
    private final String message;
    private final HttpStatus status;

    PassProductErrorCode(String code, String message, HttpStatus status) {
        this.code = code;
        this.message = message;
        this.status = status;
    }
}
