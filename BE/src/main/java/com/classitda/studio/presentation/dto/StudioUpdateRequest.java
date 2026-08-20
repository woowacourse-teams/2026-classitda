package com.classitda.studio.presentation.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import java.time.LocalTime;

public record StudioUpdateRequest(
        @Size(max = 50, message = "시설명은 50자를 넘을 수 없습니다.")
        String name,

        @Size(max = 255, message = "주소는 255자를 넘을 수 없습니다.")
        String address,

        @Size(max = 20, message = "대표 연락처는 20자를 넘을 수 없습니다.")
        String phoneNumber,

        @JsonFormat(pattern = "HH:mm:ss")
        @Schema(type = "string", format = "time", example = "09:00:00")
        LocalTime openTime,

        @JsonFormat(pattern = "HH:mm:ss")
        @Schema(type = "string", format = "time", example = "22:00:00")
        LocalTime closeTime,

        @Size(max = 500, message = "이미지 주소는 500자를 넘을 수 없습니다.")
        String imageUrl,

        String description
) {
}
