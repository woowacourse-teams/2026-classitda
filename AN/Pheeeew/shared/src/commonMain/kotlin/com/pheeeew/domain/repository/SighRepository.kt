package com.pheeeew.domain.repository

import com.pheeeew.domain.model.geo.Coordinate
import com.pheeeew.domain.model.sigh.SighBounds
import com.pheeeew.domain.model.sigh.SighPin

interface SighRepository {
    suspend fun getSighs(bounds: SighBounds): List<SighPin>

    suspend fun registerSigh(
        requestId: String,
        coordinate: Coordinate,
    ): SighPin
}
