package com.classitda.studio.presentation.dto;

import com.classitda.studio.domain.Studio;
import java.time.LocalTime;

public record StudioResponse(
        Long id,
        String name,
        String address,
        String phoneNumber,
        LocalTime openTime,
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
