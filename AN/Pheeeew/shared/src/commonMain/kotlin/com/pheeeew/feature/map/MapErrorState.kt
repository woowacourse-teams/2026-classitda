package com.pheeeew.feature.map

import com.pheeeew.feature.map.map.MapError

internal data class MapErrorState(
    val error: MapError? = null,
) {
    fun onError(error: MapError): MapErrorState = copy(error = error)

    fun onRecovered(): MapErrorState = copy(error = null)
}
