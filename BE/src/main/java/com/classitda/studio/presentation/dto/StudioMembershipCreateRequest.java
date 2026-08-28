package com.classitda.studio.presentation.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record StudioMembershipCreateRequest(
        @NotBlank(message = "소속 이름은 필수입니다.")
        @Size(max = 50, message = "소속 이름은 50자 이하여야 합니다.")
        String name,

        @NotBlank(message = "전화번호는 필수입니다.")
        @Pattern(regexp = "^010[0-9]{8}$", message = "전화번호 형식이 올바르지 않습니다.")
        String phoneNumber
) {
    public static StudioMembershipCreateRequest of(String name, String phoneNumber) {
        return new StudioMembershipCreateRequest(name, phoneNumber);
    }
}
