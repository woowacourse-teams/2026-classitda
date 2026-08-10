package com.classitda.studio.presentation.dto;

import com.classitda.member.domain.Member;
import com.classitda.studio.domain.Studio;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalTime;

public record StudioCreateRequest(
        @NotBlank(message = "시설명은 필수입니다.")
        @Size(max = 50, message = "시설명은 50자를 넘을 수 없습니다.")
        String name,

        @NotBlank(message = "주소는 필수입니다.")
        @Size(max = 255, message = "주소는 255자를 넘을 수 없습니다.")
        String address,

        @NotBlank(message = "대표 연락처는 필수입니다.")
        @Size(max = 20, message = "대표 연락처는 20자를 넘을 수 없습니다.")
        String phoneNumber,

        @NotNull(message = "운영 시작 시간은 필수입니다.")
        LocalTime openTime,

        @NotNull(message = "운영 종료 시간은 필수입니다.")
        LocalTime closeTime,

        @Size(max = 500, message = "이미지 주소는 500자를 넘을 수 없습니다.")
        String imageUrl,

        String description
) {
    public Studio toEntity(Member owner) {
        return Studio.builder()
                .owner(owner)
                .name(name)
                .address(address)
                .phoneNumber(phoneNumber)
                .openTime(openTime)
                .closeTime(closeTime)
                .imageUrl(imageUrl)
                .description(description)
                .build();
    }
}
