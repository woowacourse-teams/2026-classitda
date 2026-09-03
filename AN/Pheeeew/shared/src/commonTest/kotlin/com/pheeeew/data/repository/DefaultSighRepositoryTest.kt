@file:Suppress("NonAsciiCharacters", "ktlint:standard:multiline-expression-wrapping")

package com.pheeeew.data.repository

import com.pheeeew.data.remote.sigh.SighApi
import com.pheeeew.data.remote.sigh.dto.PointGeometryDto
import com.pheeeew.data.remote.sigh.dto.SighCreateRequestDto
import com.pheeeew.data.remote.sigh.dto.SighFeatureCollectionDto
import com.pheeeew.data.remote.sigh.dto.SighFeatureDto
import com.pheeeew.data.remote.sigh.dto.SighPropertiesDto
import com.pheeeew.domain.model.geo.Coordinate
import com.pheeeew.domain.model.sigh.SighBounds
import com.pheeeew.domain.model.sigh.SighPin
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

class DefaultSighRepositoryTest {
    private val bounds =
        SighBounds(
            minLongitude = 126.9,
            minLatitude = 37.5,
            maxLongitude = 127.1,
            maxLatitude = 37.6,
        )

    @Test
    fun `조회 응답의 Feature들을 SighPin 목록으로 변환한다`() =
        runTest {
            val api =
                RecordingSighApi(
                    getSighsResponse =
                        SighFeatureCollectionDto(
                            type = "FeatureCollection",
                            truncated = false,
                            features =
                                listOf(
                                    SighFeatureDto(
                                        type = "Feature",
                                        id = 42L,
                                        geometry =
                                            PointGeometryDto(
                                                type = "Point",
                                                coordinates = listOf(126.9780, 37.5665),
                                            ),
                                        properties = SighPropertiesDto(),
                                    ),
                                ),
                        ),
                )
            val repository = DefaultSighRepository(api)

            val result = repository.getSighs(bounds)

            assertEquals(
                listOf(
                    SighPin(
                        id = 42L,
                        coordinate =
                            Coordinate(
                                latitude = 37.5665,
                                longitude = 126.9780,
                            ),
                    ),
                ),
                result,
            )
            assertEquals(bounds, api.receivedBounds)
        }

    @Test
    fun `등록 시 서버 응답 좌표를 반환한다`() =
        runTest {
            val inputCoordinate =
                Coordinate(
                    latitude = 37.5665,
                    longitude = 126.9780,
                )
            val serverCoordinate =
                Coordinate(
                    latitude = 37.5668,
                    longitude = 126.9775,
                )
            val api =
                RecordingSighApi(
                    registerSighResponse =
                        SighFeatureDto(
                            type = "Feature",
                            id = 100L,
                            geometry =
                                PointGeometryDto(
                                    type = "Point",
                                    coordinates =
                                        listOf(
                                            serverCoordinate.longitude,
                                            serverCoordinate.latitude,
                                        ),
                                ),
                            properties = SighPropertiesDto(),
                        ),
                )
            val repository = DefaultSighRepository(api)

            val result =
                repository.registerSigh(
                    requestId = "request-123",
                    coordinate = inputCoordinate,
                )

            assertEquals(serverCoordinate, result.coordinate)
            assertEquals(100L, result.id)
            assertEquals(
                SighCreateRequestDto(
                    requestId = "request-123",
                    latitude = inputCoordinate.latitude,
                    longitude = inputCoordinate.longitude,
                ),
                api.receivedRequest,
            )
        }

    private class RecordingSighApi(
        private val getSighsResponse: SighFeatureCollectionDto =
            SighFeatureCollectionDto(
                type = "FeatureCollection",
                truncated = false,
                features = emptyList(),
            ),
        private val registerSighResponse: SighFeatureDto =
            SighFeatureDto(
                type = "Feature",
                id = 1L,
                geometry =
                    PointGeometryDto(
                        type = "Point",
                        coordinates = listOf(0.0, 0.0),
                    ),
            ),
    ) : SighApi {
        var receivedBounds: SighBounds? = null
            private set
        var receivedRequest: SighCreateRequestDto? = null
            private set

        override suspend fun getSighs(bounds: SighBounds): SighFeatureCollectionDto {
            receivedBounds = bounds
            return getSighsResponse
        }

        override suspend fun registerSigh(request: SighCreateRequestDto): SighFeatureDto {
            receivedRequest = request
            return registerSighResponse
        }
    }
}
