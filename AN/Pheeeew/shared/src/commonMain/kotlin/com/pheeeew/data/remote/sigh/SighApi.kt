package com.pheeeew.data.remote.sigh

import com.pheeeew.data.remote.sigh.dto.SighCreateRequestDto
import com.pheeeew.data.remote.sigh.dto.SighFeatureCollectionDto
import com.pheeeew.data.remote.sigh.dto.SighFeatureDto
import com.pheeeew.domain.model.sigh.SighBounds

interface SighApi {
    suspend fun getSighs(bounds: SighBounds): SighFeatureCollectionDto

    suspend fun registerSigh(request: SighCreateRequestDto): SighFeatureDto
}
