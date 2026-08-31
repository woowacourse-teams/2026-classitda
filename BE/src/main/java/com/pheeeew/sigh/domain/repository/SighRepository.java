package com.pheeeew.sigh.domain.repository;

import com.pheeeew.sigh.domain.Sigh;
import com.pheeeew.sigh.domain.repository.projection.GeneratedLocation;
import com.pheeeew.sigh.domain.repository.projection.SighMapProjection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface SighRepository extends JpaRepository<Sigh, Long> {

    Optional<Sigh> findByRequestId(UUID requestId);

    @Query(
            value = """
                    WITH bounds AS (
                        SELECT ST_MakeEnvelope(
                            :minLongitude,
                            :minLatitude,
                            :maxLongitude,
                            :maxLatitude,
                            4326
                        ) AS area
                    )
                    SELECT
                        sigh.id AS id,
                        ST_X(sigh.location) AS longitude,
                        ST_Y(sigh.location) AS latitude,
                        sigh.created_at AS "createdAt"
                    FROM sighs sigh
                    CROSS JOIN bounds
                    WHERE sigh.location && bounds.area
                      AND ST_Intersects(sigh.location, bounds.area)
                    ORDER BY sigh.created_at DESC, sigh.id DESC
                    LIMIT :limit
                    """,
            nativeQuery = true
    )
    List<SighMapProjection> findAllWithinBounds(
            @Param("minLongitude") double minLongitude,
            @Param("minLatitude") double minLatitude,
            @Param("maxLongitude") double maxLongitude,
            @Param("maxLatitude") double maxLatitude,
            @Param("limit") int limit
    );

    @Query(
            value = """
                    WITH projected_center AS (
                        SELECT ST_Transform(
                            ST_SetSRID(ST_MakePoint(:longitude, :latitude), 4326),
                            5179
                        ) AS center
                    ), shifted_location AS (
                        SELECT ST_SetSRID(
                            ST_MakePoint(
                                ST_X(center) + :eastingOffset,
                                ST_Y(center) + :northingOffset
                            ),
                            5179
                        ) AS location
                        FROM projected_center
                    ), display_location AS (
                        SELECT ST_Transform(location, 4326) AS location
                        FROM shifted_location
                    )
                    SELECT
                        ST_X(location) AS longitude,
                        ST_Y(location) AS latitude
                    FROM display_location
                    """,
            nativeQuery = true
    )
    GeneratedLocation findGeneratedLocation(
            @Param("longitude") double longitude,
            @Param("latitude") double latitude,
            @Param("eastingOffset") double eastingOffset,
            @Param("northingOffset") double northingOffset
    );
}
