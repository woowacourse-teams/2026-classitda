package com.classitda.authentication.application.sms;

public interface SmsSender {

    void send(String phoneNumber, String otp);
}
