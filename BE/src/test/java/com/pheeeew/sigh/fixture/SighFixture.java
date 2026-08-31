package com.pheeeew.sigh.fixture;

import com.pheeeew.sigh.domain.Sigh;
import java.util.UUID;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.PrecisionModel;

public final class SighFixture {

    private static final int WGS84_SRID = 4326;
    private static final GeometryFactory GEOMETRY_FACTORY =
            new GeometryFactory(new PrecisionModel(), WGS84_SRID);

    private SighFixture() {
    }

    public static Sigh.SighBuilder 기본_한숨_빌더() {
        return Sigh.builder()
                .requestId(UUID.randomUUID())
                .location(서울시청_좌표());
    }

    public static Point 서울시청_좌표() {
        return GEOMETRY_FACTORY.createPoint(new Coordinate(126.9774, 37.5669));
    }

    public static Point 서울시청_좌표(int srid) {
        Point point = new GeometryFactory().createPoint(new Coordinate(126.9774, 37.5669));
        point.setSRID(srid);
        return point;
    }
}
