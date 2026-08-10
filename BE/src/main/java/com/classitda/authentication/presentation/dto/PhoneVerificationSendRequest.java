package com.classitda.authentication.presentation.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record PhoneVerificationSendRequest(
        @NotBlank
        @Pattern(regexp = "^\\+8210[0-9]{8}$")
        String phoneNumber
) {

    public static PhoneVerificationSendRequest from(String phoneNumber) {
        return new PhoneVerificationSendRequest(phoneNumber);
    }
}
