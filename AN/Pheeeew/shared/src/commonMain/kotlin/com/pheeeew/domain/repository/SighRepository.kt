package com.pheeeew.domain.repository

import com.pheeeew.domain.model.geo.Coordinate
import com.pheeeew.domain.model.sigh.SighBounds
import com.pheeeew.domain.model.sigh.SighPin

interface SighRepository {
    suspend fun getSighs(bounds: SighBounds): List<SighPin>

    suspend fun getSighs(): List<SighPin> =
        getSighs(
            SighBounds(
                minLongitude = -180.0,
                minLatitude = -90.0,
                maxLongitude = 180.0,
                maxLatitude = 90.0,
            ),
        )

    suspend fun registerSigh(
        requestId: String,
        coordinate: Coordinate,
    ): SighPin
}
