package com.pheeeew.sigh.infra;

import static org.assertj.core.api.Assertions.assertThat;

import com.pheeeew.sigh.application.SighLocationGenerator;
import com.pheeeew.support.PostgisDataJpaTest;
import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.Point;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.simple.JdbcClient;

@PostgisDataJpaTest
class PostgisSighLocationGeneratorIntegrationTest {

    private static final int WGS84_SRID = 4326;
    private static final double GRID_HALF_SIZE_METERS = 150.0;
    private static final double SEOUL_CITY_HALL_LONGITUDE = 126.9780;
    private static final double SEOUL_CITY_HALL_LATITUDE = 37.5664;

    @Autowired
    private SighLocationGenerator sighLocationGenerator;

    @Autowired
    private JdbcClient jdbcClient;

    @Test
    void 격자_중심에서_동서와_남북_각각_150미터_안의_위치를_생성한다() {
        // given
        double centerLongitude = SEOUL_CITY_HALL_LONGITUDE;
        double centerLatitude = SEOUL_CITY_HALL_LATITUDE;

        // when
        Point location = sighLocationGenerator.generate(centerLongitude, centerLatitude);

        // then
        ProjectedOffset offset = findProjectedOffset(location, centerLongitude, centerLatitude);
        assertThat(location.getSRID()).isEqualTo(WGS84_SRID);
        assertThat(offset.easting()).isGreaterThanOrEqualTo(-GRID_HALF_SIZE_METERS)
                .isLessThan(GRID_HALF_SIZE_METERS);
        assertThat(offset.northing()).isGreaterThanOrEqualTo(-GRID_HALF_SIZE_METERS)
                .isLessThan(GRID_HALF_SIZE_METERS);
    }

    private ProjectedOffset findProjectedOffset(Point location, double centerLongitude, double centerLatitude) {
        return jdbcClient.sql("""
                        WITH projected_points AS (
                            SELECT
                                ST_Transform(
                                    ST_SetSRID(
                                        ST_MakePoint(:locationLongitude, :locationLatitude),
                                        4326
                                    ),
                                    5179
                                ) AS display_location,
                                ST_Transform(
                                    ST_SetSRID(
                                        ST_MakePoint(:centerLongitude, :centerLatitude),
                                        4326
                                    ),
                                    5179
                                ) AS center
                        )
                        SELECT
                            ST_X(display_location) - ST_X(center) AS easting,
                            ST_Y(display_location) - ST_Y(center) AS northing
                        FROM projected_points
                        """)
                .param("locationLongitude", location.getX())
                .param("locationLatitude", location.getY())
                .param("centerLongitude", centerLongitude)
                .param("centerLatitude", centerLatitude)
                .query((resultSet, rowNumber) -> ProjectedOffset.of(
                        resultSet.getDouble("easting"),
                        resultSet.getDouble("northing")
                ))
                .single();
    }

    private record ProjectedOffset(double easting, double northing) {

        private static ProjectedOffset of(double easting, double northing) {
            return new ProjectedOffset(easting, northing);
        }
    }
}
