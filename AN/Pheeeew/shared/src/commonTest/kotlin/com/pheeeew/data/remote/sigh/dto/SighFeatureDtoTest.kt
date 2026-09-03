@file:Suppress("NonAsciiCharacters")

package com.pheeeew.data.remote.sigh.dto

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class SighFeatureDtoTest {
    @Test
    fun `GeoJSON 좌표를 앱 Coordinate로 변환한다`() {
        val feature =
            SighFeatureDto(
                type = "Feature",
                id = 42L,
                geometry =
                    PointGeometryDto(
                        type = "Point",
                        coordinates = listOf(126.9780, 37.5665),
                    ),
                properties = null,
            )

        val result = feature.toDomain()

        assertEquals(42L, result.id)
        assertEquals(37.5665, result.coordinate.latitude)
        assertEquals(126.9780, result.coordinate.longitude)
    }

    @Test
    fun `좌표가 2개 미만이면 예외를 던진다`() {
        val feature =
            SighFeatureDto(
                type = "Feature",
                id = 42L,
                geometry =
                    PointGeometryDto(
                        type = "Point",
                        coordinates = listOf(126.9780),
                    ),
                properties = null,
            )

        assertFailsWith<IllegalArgumentException> {
            feature.toDomain()
        }
    }
}
