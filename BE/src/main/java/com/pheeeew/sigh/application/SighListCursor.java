package com.pheeeew.sigh.application;

import java.time.Instant;
import java.util.Objects;

public record SighListCursor(
        double minLongitude,
        double minLatitude,
        double maxLongitude,
        double maxLatitude,
        Instant snapshotAt,
        Instant lastCreatedAt,
        long lastId
) {

    public SighListCursor {
        validateLongitude(minLongitude);
        validateLongitude(maxLongitude);
        validateLatitude(minLatitude);
        validateLatitude(maxLatitude);
        if (minLongitude == maxLongitude) {
            throw new IllegalArgumentException("검색 영역의 두 경도는 달라야 합니다.");
        }
        if (minLatitude >= maxLatitude) {
            throw new IllegalArgumentException("검색 영역의 최소 위도는 최대 위도보다 작아야 합니다.");
        }

        Objects.requireNonNull(snapshotAt);
        Objects.requireNonNull(lastCreatedAt);
        if (lastCreatedAt.isAfter(snapshotAt)) {
            throw new IllegalArgumentException("마지막 한숨 생성 시각은 스냅샷 시각보다 늦을 수 없습니다.");
        }
        if (lastId < 1) {
            throw new IllegalArgumentException("마지막 한숨 ID는 1 이상이어야 합니다.");
        }
    }

    public static SighListCursor of(
            double minLongitude,
            double minLatitude,
            double maxLongitude,
            double maxLatitude,
            Instant snapshotAt,
            Instant lastCreatedAt,
            long lastId
    ) {
        return new SighListCursor(
                minLongitude,
                minLatitude,
                maxLongitude,
                maxLatitude,
                snapshotAt,
                lastCreatedAt,
                lastId
        );
    }

    private static void validateLongitude(double longitude) {
        if (!Double.isFinite(longitude) || longitude < -180.0 || longitude > 180.0) {
            throw new IllegalArgumentException("경도는 -180 이상 180 이하여야 합니다.");
        }
    }

    private static void validateLatitude(double latitude) {
        if (!Double.isFinite(latitude) || latitude < -90.0 || latitude > 90.0) {
            throw new IllegalArgumentException("위도는 -90 이상 90 이하여야 합니다.");
        }
    }

}
