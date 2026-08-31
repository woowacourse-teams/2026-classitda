package com.pheeeew.sigh.infra;

import com.pheeeew.sigh.application.SighLocationGenerator;
import com.pheeeew.sigh.domain.repository.SighRepository;
import com.pheeeew.sigh.domain.repository.projection.GeneratedLocation;
import java.util.concurrent.ThreadLocalRandom;
import lombok.RequiredArgsConstructor;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.PrecisionModel;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class PostgisSighLocationGenerator implements SighLocationGenerator {

    private static final int WGS84_SRID = 4326;
    private static final double GRID_HALF_SIZE_METERS = 150.0;
    private static final GeometryFactory GEOMETRY_FACTORY = new GeometryFactory(new PrecisionModel(), WGS84_SRID);

    private final SighRepository sighRepository;

    @Override
    public Point generate(double longitude, double latitude) {
        GeneratedLocation location
                = sighRepository.findGeneratedLocation(longitude, latitude, randomOffset(), randomOffset());

        return GEOMETRY_FACTORY.createPoint(
                new Coordinate(location.getLongitude(), location.getLatitude())
        );
    }

    private double randomOffset() {
        return ThreadLocalRandom.current().nextDouble(-GRID_HALF_SIZE_METERS, GRID_HALF_SIZE_METERS);
    }
}
