package com.classitda.studio.presentation.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.classitda.studio.domain.Studio;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalTime;

public record StudioResponse(
        Long id,
        String name,
        AddressResponse address,
        String phoneNumber,
        @JsonFormat(pattern = "HH:mm")
        @Schema(type = "string", format = "time", example = "09:00")
        LocalTime openTime,
        @JsonFormat(pattern = "HH:mm")
        @Schema(type = "string", format = "time", example = "22:00")
        LocalTime closeTime,
        String image,
        String description
) {
    public static StudioResponse of(Studio studio, String image) {
        return new StudioResponse(
                studio.getId(),
                studio.getName(),
                AddressResponse.from(studio.getAddress()),
                studio.getPhoneNumber(),
                studio.getOpenTime(),
                studio.getCloseTime(),
                image,
                studio.getDescription()
        );
    }
}
