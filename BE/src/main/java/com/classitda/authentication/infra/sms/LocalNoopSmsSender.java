package com.classitda.authentication.infra.sms;

import com.classitda.authentication.application.phone.SmsSender;

public class LocalNoopSmsSender implements SmsSender {

    @Override
    public void send(String phoneNumber, String otp) {
    }
}
