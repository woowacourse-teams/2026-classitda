package com.pheeeew.data.remote.sigh.dto

import kotlinx.serialization.Serializable

/**
 * GeoJSON Feature의 위치 정보를 나타내는 Geometry DTO
 *
 * 서버 응답의 coordinates는 GeoJSON 표준에 따라
 * [longitude, latitude] 순서입니다.
 */
@Serializable
data class PointGeometryDto(
    val type: String,
    val coordinates: List<Double>,
)
