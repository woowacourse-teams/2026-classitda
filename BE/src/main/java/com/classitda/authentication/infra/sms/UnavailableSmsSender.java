package com.classitda.authentication.infra.sms;

import com.classitda.authentication.application.sms.SmsSender;
import com.classitda.authentication.exception.AuthErrorCode;
import com.classitda.authentication.exception.AuthException;

/** 실제 SENS provider가 없는 현재 prod 환경에서 발송 성공을 가장하지 않고 명시적으로 실패한다. */
public class UnavailableSmsSender implements SmsSender {

    @Override
    public void send(String phoneNumber, String otp) {
        throw new AuthException(AuthErrorCode.PHONE_DELIVERY_FAILED);
    }
}
