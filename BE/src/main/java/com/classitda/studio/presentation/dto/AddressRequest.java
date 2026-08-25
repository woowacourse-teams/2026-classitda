package com.classitda.studio.presentation.dto;

import com.classitda.studio.domain.Address;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record AddressRequest(
        @Schema(description = "우편번호", example = "13529")
        @NotBlank(message = "우편번호는 필수입니다.")
        @Pattern(regexp = "\\d{5}", message = "우편번호는 5자리 숫자여야 합니다.")
        String zonecode,

        @Schema(description = "도로명 주소", example = "경기 성남시 분당구 판교역로 166")
        @NotBlank(message = "도로명 주소는 필수입니다.")
        @Size(max = 255, message = "도로명 주소는 255자를 넘을 수 없습니다.")
        String roadAddress,

        @Schema(description = "지번 주소", example = "경기 성남시 분당구 백현동 532")
        @Size(max = 255, message = "지번 주소는 255자를 넘을 수 없습니다.")
        String jibunAddress,

        @Schema(description = "건물명", example = "카카오 판교 아지트")
        @Size(max = 100, message = "건물명은 100자를 넘을 수 없습니다.")
        String buildingName,

        @Schema(description = "상세 주소", example = "3층 301호")
        @Size(max = 100, message = "상세 주소는 100자를 넘을 수 없습니다.")
        String detailAddress
) {
    public static AddressRequest of(
            String zonecode,
            String roadAddress,
            String jibunAddress,
            String buildingName,
            String detailAddress
    ) {
        return new AddressRequest(zonecode, roadAddress, jibunAddress, buildingName, detailAddress);
    }

    public Address toAddress() {
        return Address.of(zonecode, roadAddress, jibunAddress, buildingName, detailAddress);
    }
}
