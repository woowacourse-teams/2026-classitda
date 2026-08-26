package com.classitda.studio.presentation.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record StudioMembershipUpdateRequest(
        @Schema(description = "시설 안에서 부르는 이름", example = "김민수")
        @NotBlank(message = "소속 이름은 필수입니다.")
        @Size(max = 50, message = "소속 이름은 50자 이하여야 합니다.")
        String name,

        @Schema(description = "시설이 관리하는 연락처. 가입한 회원은 바꿀 수 없습니다.", example = "01012345678")
        @NotBlank(message = "전화번호는 필수입니다.")
        @Pattern(regexp = "^010[0-9]{8}$", message = "전화번호 형식이 올바르지 않습니다.")
        String phoneNumber
) {
    public static StudioMembershipUpdateRequest of(String name, String phoneNumber) {
        return new StudioMembershipUpdateRequest(name, phoneNumber);
    }
}
