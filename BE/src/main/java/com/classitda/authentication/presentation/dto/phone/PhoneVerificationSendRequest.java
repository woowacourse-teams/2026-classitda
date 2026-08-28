package com.classitda.authentication.presentation.dto.phone;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record PhoneVerificationSendRequest(
        @NotBlank
        @Pattern(regexp = "^010[0-9]{8}$")
        String phoneNumber
) {

    public static PhoneVerificationSendRequest from(String phoneNumber) {
        return new PhoneVerificationSendRequest(phoneNumber);
    }
}
