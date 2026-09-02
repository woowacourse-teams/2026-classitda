@file:Suppress("NonAsciiCharacters")

package com.pheeeew.core.geo

import com.pheeeew.domain.model.geo.Coordinate
import kotlin.math.abs
import kotlin.math.floor
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class Epsg5179GridTest {
    @Test
    fun `같은 300m 격자 안의 좌표는 같은 중심을 반환한다`() {
        val first = Coordinate(latitude = 37.5665, longitude = 126.9780)
        val second = Coordinate(latitude = 37.5668, longitude = 126.9782)

        assertEquals(first.toGridCenter(), second.toGridCenter())
    }

    @Test
    fun `결과 좌표를 EPSG 5179로 재투영하면 각 축이 격자 중심이다`() {
        val center = Coordinate(latitude = 37.5665, longitude = 126.9780).toGridCenter()
        val projected = Epsg5179Projection.forward(center)

        assertEquals(150.0, projected.easting - floor(projected.easting / GRID_SIZE) * GRID_SIZE, 0.01)
        assertEquals(150.0, projected.northing - floor(projected.northing / GRID_SIZE) * GRID_SIZE, 0.01)
    }

    @Test
    fun `격자 중심은 원본과 같지 않고 300m 이내의 같은 격자에 있다`() {
        val original = Coordinate(latitude = 37.5665, longitude = 126.9780)
        val center = original.toGridCenter()
        val originalProjected = Epsg5179Projection.forward(original)
        val centerProjected = Epsg5179Projection.forward(center)

        assertTrue(original != center)
        assertTrue(abs(originalProjected.easting - centerProjected.easting) <= GRID_SIZE / 2.0 + 0.01)
        assertTrue(abs(originalProjected.northing - centerProjected.northing) <= GRID_SIZE / 2.0 + 0.01)
    }

    @Test
    fun `유효하지 않은 좌표는 거부한다`() {
        assertFailsWith<IllegalArgumentException> {
            Coordinate(latitude = 91.0, longitude = 127.0).toGridCenter()
        }
        assertFailsWith<IllegalArgumentException> {
            Coordinate(latitude = 37.0, longitude = Double.NaN).toGridCenter()
        }
    }

    private companion object {
        const val GRID_SIZE = 300.0
    }
}
