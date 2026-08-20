package com.classitda.studio.presentation.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.classitda.studio.domain.Studio;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalTime;

public record StudioResponse(
        Long id,
        String name,
        String address,
        String phoneNumber,
        @JsonFormat(pattern = "HH:mm:ss")
        @Schema(type = "string", format = "time", example = "09:00:00")
        LocalTime openTime,
        @JsonFormat(pattern = "HH:mm:ss")
        @Schema(type = "string", format = "time", example = "22:00:00")
        LocalTime closeTime,
        String imageUrl,
        String description
) {
    public static StudioResponse from(Studio studio) {
        return new StudioResponse(
                studio.getId(),
                studio.getName(),
                studio.getAddress(),
                studio.getPhoneNumber(),
                studio.getOpenTime(),
                studio.getCloseTime(),
                studio.getImageUrl(),
                studio.getDescription()
        );
    }
}
