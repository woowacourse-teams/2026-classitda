package com.classitda.member.exception;

import com.classitda.common.exception.ErrorCode;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum MemberErrorCode implements ErrorCode {

    MEMBER_NAME_REQUIRED("MEMBER-001", "회원 이름은 필수입니다.", HttpStatus.BAD_REQUEST),
    MEMBER_PHONE_NUMBER_INVALID("MEMBER-002", "휴대전화 번호는 +8210으로 시작하는 E.164 형식이어야 합니다.", HttpStatus.BAD_REQUEST);

    private final String code;
    private final String message;
    private final HttpStatus status;

    MemberErrorCode(String code, String message, HttpStatus status) {
        this.code = code;
        this.message = message;
        this.status = status;
    }
}
