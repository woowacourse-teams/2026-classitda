package com.pheeeew.sigh.application;

import com.pheeeew.sigh.domain.Sigh;
import java.time.Instant;

public record SighDetailResult(
        Long id,
        double longitude,
        double latitude,
        Instant createdAt,
        String memo,
        String nickname
) {

    public static SighDetailResult from(Sigh sigh) {
        return new SighDetailResult(
                sigh.getId(),
                sigh.getLongitude(),
                sigh.getLatitude(),
                sigh.getCreatedAt(),
                sigh.getMemo(),
                sigh.getNickname()
        );
    }
}
