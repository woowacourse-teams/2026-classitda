package com.pheeeew.domain.repository

import com.pheeeew.domain.model.location.LocationState
import kotlinx.coroutines.flow.StateFlow

interface LocationRepository {
    val locationState: StateFlow<LocationState>

    suspend fun refreshCurrentLocation()
}
