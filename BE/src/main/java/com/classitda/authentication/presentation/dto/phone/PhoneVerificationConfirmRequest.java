package com.classitda.authentication.presentation.dto.phone;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record PhoneVerificationConfirmRequest(
        @NotBlank
        @Pattern(regexp = "^[0-9]{6}$")
        String otp
) {

    public static PhoneVerificationConfirmRequest from(String otp) {
        return new PhoneVerificationConfirmRequest(otp);
    }
}
