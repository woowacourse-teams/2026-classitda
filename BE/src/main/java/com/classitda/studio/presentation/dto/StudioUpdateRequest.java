package com.classitda.studio.presentation.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;
import java.time.LocalTime;

public record StudioUpdateRequest(
        @Size(max = 50, message = "시설명은 50자를 넘을 수 없습니다.")
        String name,

        @Valid
        AddressRequest address,

        @Size(max = 20, message = "대표 연락처는 20자를 넘을 수 없습니다.")
        String phoneNumber,

        @JsonFormat(pattern = "HH:mm")
        @Schema(type = "string", format = "time", example = "09:00")
        LocalTime openTime,

        @JsonFormat(pattern = "HH:mm")
        @Schema(type = "string", format = "time", example = "22:00")
        LocalTime closeTime,

        @Schema(description = "업로드 URL 발급으로 받은 대표 이미지의 objectKey", example = "studio-images/9f1c2b7e.jpg")
        String image,

        String description
) {
}
