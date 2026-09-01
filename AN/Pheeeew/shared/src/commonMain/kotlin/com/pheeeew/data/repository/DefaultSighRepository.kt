package com.pheeeew.data.repository

import com.pheeeew.data.remote.sigh.SighApi
import com.pheeeew.data.remote.sigh.dto.SighCreateRequestDto
import com.pheeeew.data.remote.sigh.dto.toDomain
import com.pheeeew.domain.model.geo.Coordinate
import com.pheeeew.domain.model.sigh.SighBounds
import com.pheeeew.domain.model.sigh.SighPin
import com.pheeeew.domain.repository.SighRepository

class DefaultSighRepository(
    private val sighApi: SighApi,
) : SighRepository {
    override suspend fun getSighs(bounds: SighBounds): List<SighPin> =
        sighApi
            .getSighs(bounds)
            .features
            .map { it.toDomain() }

    override suspend fun registerSigh(
        requestId: String,
        coordinate: Coordinate,
    ): SighPin {
        val request =
            SighCreateRequestDto(
                requestId = requestId,
                latitude = coordinate.latitude,
                longitude = coordinate.longitude,
            )
        return sighApi
            .registerSigh(request)
            .toDomain()
    }
}
