package com.pheeeew.domain.model.location

import com.pheeeew.domain.model.geo.Coordinate

/**
 * GPS로 획득한 사용자의 현재 위치입니다.
 *
 * @property coordinate 위도와 경도
 * @property accuracyMeters GPS 위치의 예상 오차 범위(미터)
 * @property capturedAtMillis 위치를 획득한 시각(epoch milliseconds)
 */
data class CurrentLocation(
    val coordinate: Coordinate,
    val accuracyMeters: Float,
    val capturedAtMillis: Long,
)
