package com.classitda.member.exception;

import com.classitda.common.exception.ErrorCode;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum MemberErrorCode implements ErrorCode {

    MEMBER_NAME_REQUIRED("MEMBER-001", "회원 이름은 필수입니다.", HttpStatus.BAD_REQUEST),
    MEMBER_PHONE_NUMBER_INVALID("MEMBER-002", "휴대전화 번호는 010으로 시작하는 숫자 11자리여야 합니다.", HttpStatus.BAD_REQUEST),
    MEMBER_NAME_TOO_LONG("MEMBER-003", "회원 이름은 50자 이하여야 합니다.", HttpStatus.BAD_REQUEST),
    MEMBER_WITHDRAWAL_REQUESTED_AT_REQUIRED("MEMBER-004", "회원 탈퇴 요청 시각은 필수입니다.", HttpStatus.BAD_REQUEST),
    MEMBER_CLEANUP_OCCURRED_AT_REQUIRED("MEMBER-005", "회원 개인정보 정리 시각은 필수입니다.", HttpStatus.BAD_REQUEST),
    MEMBER_WITHDRAWAL_REQUIRED("MEMBER-006", "탈퇴를 요청하지 않은 회원의 개인정보는 정리할 수 없습니다.", HttpStatus.CONFLICT),
    MEMBER_CLEANUP_NOT_DUE("MEMBER-007", "회원 개인정보 정리 예정 시각이 지나지 않았습니다.", HttpStatus.CONFLICT),
    MEMBER_NOT_FOUND("MEMBER-008", "회원을 찾을 수 없습니다.", HttpStatus.NOT_FOUND),
    MEMBER_WITHDRAWAL_BLOCKED_BY_OWNED_STUDIO("MEMBER-009", "시설 대표는 탈퇴할 수 없습니다.", HttpStatus.CONFLICT),

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
