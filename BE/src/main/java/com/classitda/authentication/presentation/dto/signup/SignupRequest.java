package com.classitda.authentication.presentation.dto.signup;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.util.List;

public record SignupRequest(
        @NotBlank
        @Size(max = 50)
        String name,
        @NotNull
        @Size(min = 1)
        List<@NotNull @Positive Long> agreedTermIds
) {

    public static SignupRequest of(String name, List<Long> agreedTermIds) {
        return new SignupRequest(name, agreedTermIds);
    }
}
