package com.pheeeew.sigh.presentation.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public record SighFeature<P>(
        @Schema(example = "Feature")
        String type,

        @Schema(description = "한숨 ID", example = "42")
        Long id,

        PointGeometry geometry,

        P properties
) {

    private static final String FEATURE_TYPE = "Feature";

    public static <P> SighFeature<P> of(
            Long id,
            double longitude,
            double latitude,
            P properties
    ) {
        return new SighFeature<>(
                FEATURE_TYPE,
                id,
                PointGeometry.of(longitude, latitude),
                properties
        );
    }
}
