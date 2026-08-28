package com.classitda.studio.presentation.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.classitda.member.domain.Member;
import com.classitda.studio.domain.Studio;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalTime;

public record StudioCreateRequest(
        @NotBlank(message = "시설명은 필수입니다.")
        @Size(max = 50, message = "시설명은 50자를 넘을 수 없습니다.")
        String name,

        @Valid
        @NotNull(message = "주소는 필수입니다.")
        AddressRequest address,

        @NotBlank(message = "대표 연락처는 필수입니다.")
        @Size(max = 20, message = "대표 연락처는 20자를 넘을 수 없습니다.")
        String phoneNumber,

        @NotNull(message = "운영 시작 시간은 필수입니다.")
        @JsonFormat(pattern = "HH:mm")
        @Schema(type = "string", format = "time", example = "09:00")
        LocalTime openTime,

        @NotNull(message = "운영 종료 시간은 필수입니다.")
        @JsonFormat(pattern = "HH:mm")
        @Schema(type = "string", format = "time", example = "22:00")
        LocalTime closeTime,

        @Schema(description = "업로드 URL 발급으로 받은 대표 이미지의 objectKey", example = "studio-images/9f1c2b7e.jpg")
        String image,

        String description
) {
    public Studio toEntity(Member owner) {
        return Studio.builder()
                .owner(owner)
                .name(name)
                .address(address.toAddress())
                .phoneNumber(phoneNumber)
                .imageObjectKey(image)
                .openTime(openTime)
                .closeTime(closeTime)
                .description(description)
                .build();
    }
}
