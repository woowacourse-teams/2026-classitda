package com.classitda.member.exception;

import com.classitda.common.exception.ClassitdaException;

public class MemberException extends ClassitdaException {

    public MemberException(MemberErrorCode errorCode) {
        super(errorCode);
    }
}
