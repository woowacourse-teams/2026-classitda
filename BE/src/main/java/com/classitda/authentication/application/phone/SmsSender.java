package com.classitda.authentication.application.phone;

public interface SmsSender {

    void send(String phoneNumber, String otp);
}
