package com.pheeeew.data.remote.sigh.dto

import com.pheeeew.domain.model.geo.Coordinate
import com.pheeeew.domain.model.sigh.SighPin
import kotlinx.serialization.Serializable

/**
 * 서버가 반환하는 한숨 하나를 나타내는 GeoJSON Feature DTO
 *
 * [id]는 서버가 부여한 한숨 ID입니다.
 * [geometry]에는 서버가 계산한 최종 표시 좌표가 들어 있습니다.
 */

@Serializable
data class SighFeatureDto(
    val type: String,
    val id: Long,
    val geometry: PointGeometryDto,
    val properties: SighPropertiesDto? = null,
)

fun SighFeatureDto.toDomain(): SighPin {
    require(geometry.coordinates.size >= 2) {
        "GeoJSON Point 좌표가 올바르지 않습니다."
    }

    val longitude = geometry.coordinates[0]
    val latitude = geometry.coordinates[1]

    return SighPin(
        id = id,
        coordinate =
            Coordinate(
                latitude = latitude,
                longitude = longitude,
            ),
    )
}
