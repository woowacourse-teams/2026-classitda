package com.classitda.studio.presentation.dto;

import jakarta.validation.constraints.Size;
import java.time.LocalTime;

public record StudioUpdateRequest(
        @Size(max = 50, message = "시설명은 50자를 넘을 수 없습니다.")
        String name,

        @Size(max = 255, message = "주소는 255자를 넘을 수 없습니다.")
        String address,

        @Size(max = 20, message = "대표 연락처는 20자를 넘을 수 없습니다.")
        String phoneNumber,

        LocalTime openTime,

        LocalTime closeTime,

        @Size(max = 500, message = "이미지 주소는 500자를 넘을 수 없습니다.")
        String imageUrl,

        String description
) {
}
