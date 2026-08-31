package com.pheeeew.sigh.domain;

import com.pheeeew.common.domain.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.Objects;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.locationtech.jts.geom.Point;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "sighs")
@Entity
public class Sigh extends BaseEntity {

    private static final int WGS84_SRID = 4326;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "request_id", nullable = false, updatable = false)
    private UUID requestId;

    @Getter(AccessLevel.NONE)
    @Column(nullable = false, updatable = false, columnDefinition = "geometry(Point,4326)")
    private Point location;

    @Builder
    private Sigh(UUID requestId, Point location) {
        this.requestId = Objects.requireNonNull(requestId);
        this.location = requireWgs84Point(location);
    }

    public double getLongitude() {
        return location.getX();
    }

    public double getLatitude() {
        return location.getY();
    }

    private Point requireWgs84Point(Point location) {
        Objects.requireNonNull(location);
        if (location.isEmpty() || location.getSRID() != WGS84_SRID) {
            throw new IllegalArgumentException("위치는 비어 있지 않은 WGS84(SRID 4326) 점 좌표여야 합니다.");
        }
        return location;
    }
}
