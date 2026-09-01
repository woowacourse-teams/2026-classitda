package com.pheeeew.data.remote.sigh.dto

import kotlinx.serialization.Serializable

/**
 * 지도 영역 조회 결과를 나타내는 GeoJSON FeatureCollection DTO
 *
 * [features]에는 현재 지도 영역에 포함된 한숨 목록이 들어 있습니다.
 * [truncated]가 true이면 결과가 최대 반환 개수를 초과하여
 * 일부 데이터만 반환되었음을 의미합니다.
 */
@Serializable
data class SighFeatureCollectionDto(
    val type: String,
    val truncated: Boolean,
    val features: List<SighFeatureDto>,
)
