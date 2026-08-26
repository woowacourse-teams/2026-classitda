package com.classitda.member.presentation.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record MyNameUpdateRequest(
        @Schema(description = "변경할 회원 이름", example = "김클래스")
        @NotBlank(message = "회원 이름은 필수입니다.")
        @Size(max = 50, message = "회원 이름은 50자 이하여야 합니다.")
        String name
) {
    public static MyNameUpdateRequest from(String name) {
        return new MyNameUpdateRequest(name);
    }
}
