package com.classitda.authentication.exception;

import com.classitda.common.exception.ErrorCode;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum AuthErrorCode implements ErrorCode {

    AUTHENTICATION_REQUIRED("AUTH-001", "인증이 필요합니다.", HttpStatus.UNAUTHORIZED),
    ACCESS_DENIED("AUTH-002", "접근 권한이 없습니다.", HttpStatus.FORBIDDEN),
    AUTH_ACCOUNT_MEMBER_ID_INVALID("AUTH-003", "회원 ID는 1 이상이어야 합니다.", HttpStatus.BAD_REQUEST),
    AUTH_ACCOUNT_PROVIDER_REQUIRED("AUTH-004", "OAuth 제공자는 필수입니다.", HttpStatus.BAD_REQUEST),
    AUTH_ACCOUNT_PROVIDER_SUBJECT_REQUIRED("AUTH-005", "OAuth 제공자 사용자 식별자는 필수입니다.", HttpStatus.BAD_REQUEST),
    GOOGLE_ID_TOKEN_INVALID("AUTH-006", "Google ID 토큰이 유효하지 않습니다.", HttpStatus.UNAUTHORIZED),
    AUTH_ACCOUNT_PROVIDER_EMAIL_INVALID("AUTH-007", "OAuth 제공자 이메일은 비어 있지 않고 254자 이하여야 합니다.", HttpStatus.BAD_REQUEST),
    PHONE_ALREADY_REGISTERED("PHONE-001", "이미 가입된 휴대전화 번호입니다.", HttpStatus.CONFLICT),
    PHONE_RESEND_COOLDOWN("PHONE-002", "인증번호 재발송은 잠시 후 다시 시도해 주세요.", HttpStatus.TOO_MANY_REQUESTS),
    PHONE_VERIFICATION_UNAVAILABLE("PHONE-003", "인증 요청이 만료되었거나 이미 처리되어 유효하지 않습니다.", HttpStatus.GONE),
    PHONE_OTP_INVALID("PHONE-004", "인증번호가 올바르지 않습니다.", HttpStatus.BAD_REQUEST),
    PHONE_OTP_ATTEMPTS_EXCEEDED("PHONE-005", "인증번호 입력 가능 횟수를 초과했습니다. 다시 인증해 주세요.", HttpStatus.TOO_MANY_REQUESTS),
    PHONE_VERIFICATION_SESSION_MISMATCH("PHONE-006", "현재 가입 세션의 인증 요청이 아닙니다.", HttpStatus.FORBIDDEN),
    PHONE_DELIVERY_FAILED("PHONE-007", "문자 인증번호를 발송할 수 없습니다.", HttpStatus.BAD_GATEWAY);

    private final String code;
    private final String message;
    private final HttpStatus status;

    AuthErrorCode(String code, String message, HttpStatus status) {
        this.code = code;
        this.message = message;
        this.status = status;
    }
}
