package com.pheeeew.sigh.presentation.dto;

import com.pheeeew.sigh.application.SighMapItem;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;
import java.util.List;

public record SighResponse(
        @Schema(example = "Feature")
        String type,

        @Schema(description = "한숨 ID", example = "42")
        Long id,

        PointGeometry geometry,

        SighProperties properties
) {

    private static final String FEATURE_TYPE = "Feature";

    public static SighResponse from(SighMapItem sigh) {
        return new SighResponse(
                FEATURE_TYPE,
                sigh.id(),
                PointGeometry.of(sigh.longitude(), sigh.latitude()),
                SighProperties.from(sigh.createdAt())
        );
    }

    public record PointGeometry(
            @Schema(example = "Point")
            String type,

            @Schema(description = "GeoJSON 좌표. 경도, 위도 순서입니다.", example = "[126.9774, 37.5669]")
            List<Double> coordinates
    ) {

        private static final String POINT_TYPE = "Point";

        public static PointGeometry of(double longitude, double latitude) {
            return new PointGeometry(POINT_TYPE, List.of(longitude, latitude));
        }
    }

    public record SighProperties(
            @Schema(description = "한숨 생성 시각", example = "2026-08-31T10:30:00Z")
            Instant createdAt
    ) {

        public static SighProperties from(Instant createdAt) {
            return new SighProperties(createdAt);
        }
    }
}
