package com.pheeeew.sigh.domain.repository;

import com.pheeeew.sigh.domain.Sigh;
import com.pheeeew.sigh.domain.repository.projection.GeneratedLocation;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface SighRepository extends JpaRepository<Sigh, Long> {

    Optional<Sigh> findByRequestId(UUID requestId);

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
