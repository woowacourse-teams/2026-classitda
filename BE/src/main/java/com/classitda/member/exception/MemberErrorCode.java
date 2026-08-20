package com.classitda.member.exception;

import com.classitda.common.exception.ErrorCode;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum MemberErrorCode implements ErrorCode {

    MEMBER_NAME_REQUIRED("MEMBER-001", "회원 이름은 필수입니다.", HttpStatus.BAD_REQUEST),
    MEMBER_PHONE_NUMBER_INVALID("MEMBER-002", "휴대전화 번호는 010으로 시작하는 숫자 11자리여야 합니다.", HttpStatus.BAD_REQUEST),
    MEMBER_NAME_TOO_LONG("MEMBER-003", "회원 이름은 50자 이하여야 합니다.", HttpStatus.BAD_REQUEST),

    REQUIRED_TERM_AGREEMENT_MISSING("TERM-001", "필수 약관에 모두 동의해야 합니다.", HttpStatus.BAD_REQUEST),
    TERM_NOT_FOUND("TERM-002", "존재하지 않는 약관이 포함되어 있습니다.", HttpStatus.BAD_REQUEST),
    TERM_ID_DUPLICATED("TERM-003", "중복된 약관 ID가 포함되어 있습니다.", HttpStatus.BAD_REQUEST),
    TERM_STALE("TERM-004", "약관이 변경되었습니다. 최신 약관을 다시 확인해 주세요.", HttpStatus.CONFLICT);

    private final String code;
    private final String message;
    private final HttpStatus status;

    MemberErrorCode(String code, String message, HttpStatus status) {
        this.code = code;
        this.message = message;
        this.status = status;
    }
}
