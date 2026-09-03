package com.pheeeew.sigh.application.dto;

import com.pheeeew.sigh.domain.Sigh;
import java.time.Instant;

public record SighSaveResult(
        Long id,
        double longitude,
        double latitude,
        Instant createdAt,
        String memo,
        String nickname,
        boolean created
) {

    public static SighSaveResult of(Sigh sigh, boolean created) {
        return new SighSaveResult(
                sigh.getId(),
                sigh.getLongitude(),
                sigh.getLatitude(),
                sigh.getCreatedAt(),
                sigh.getMemo(),
                sigh.getNickname(),
                created
        );
    }
}
