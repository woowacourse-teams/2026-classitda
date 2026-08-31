@file:Suppress("NonAsciiCharacters")

package com.pheeeew.fake

import com.pheeeew.domain.exception.ApiException
import com.pheeeew.domain.model.geo.Coordinate
import com.pheeeew.domain.model.sigh.SighPin
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertSame

class FakeSighRepositoryTest {
    private lateinit var repository: FakeSighRepository

    @BeforeTest
    fun setUp() {
        repository = FakeSighRepository()
    }

    @Test
    fun `설정한 한숨 목록을 반환한다`() = runTest {
        val expected =
            listOf(
                SighPin(
                    id = 1L,
                    coordinate = Coordinate(
                        latitude = 37.5665,
                        longitude = 126.9780,
                    ),
                ),
            )
        repository.setGetSighsSuccess(expected)

        val actual = repository.getSighs()
        assertEquals(expected, actual)
        assertEquals(1, repository.getSighsCallCount)
    }

    @Test
    fun `조회 실패로 설정하면 ApiException을 던진다`() = runTest {
        val expectedException =
            ApiException.Network(
                code = "NETWORK_ERROR",
                message = "네트워크 오류",
            )
        repository.setGetSighsFailure(expectedException)

        val actualException =
            assertFailsWith<ApiException.Network> {
                repository.getSighs()
            }

        assertSame(expectedException, actualException)
        assertEquals(1, repository.getSighsCallCount)
    }

    @Test
    fun `등록 결과와 전달받은 인자를 기록한다`() = runTest {
        val requestId = "request-123"
        val coordinate =
            Coordinate(
                latitude = 37.5665,
                longitude = 126.9780,
            )
        val expected =
            SighPin(
                id = 10L,
                coordinate = coordinate,
            )
        repository.setRegisterSighSuccess(expected)

        val actual = repository.registerSigh(requestId, coordinate)

        assertEquals(expected, actual)
        assertEquals(listOf(requestId), repository.receivedRequestIds)
        assertEquals(listOf(coordinate), repository.receivedCoordinates)
        assertEquals(1, repository.registerSighCallCount)
    }

    @Test
    fun `등록 실패 시에도 인자와 호출 횟수를 기록한다`() = runTest {
        val requestId = "request-456"
        val coordinate =
            Coordinate(
                latitude = 35.1796,
                longitude = 129.0756,
            )
        val expectedException =
            ApiException.Conflict(
                code = "DUPLICATED_REQUEST",
                message = "중복 요청",
            )
        repository.setRegisterSighFailure(expectedException)

        val actualException =
            assertFailsWith<ApiException.Conflict> {
                repository.registerSigh(requestId, coordinate)
            }

        assertSame(expectedException, actualException)
        assertEquals(listOf(requestId), repository.receivedRequestIds)
        assertEquals(listOf(coordinate), repository.receivedCoordinates)
        assertEquals(1, repository.registerSighCallCount)
    }

    @Test
    fun `조회와 등록 호출 횟수를 누락한다`() = runTest {
        val coordinate = Coordinate(37.0, 127.0)
        val sighPin = SighPin(id = 1L, coordinate = coordinate)

        repository.setGetSighsSuccess(emptyList())
        repository.setRegisterSighSuccess(sighPin)

        repeat(2) {
            repository.getSighs()
        }
        repeat(3) { index ->
            repository.registerSigh(
                requestId = "request-$index",
                coordinate = coordinate,
            )
        }

        assertEquals(2, repository.getSighsCallCount)
        assertEquals(3, repository.registerSighCallCount)
        assertEquals(
            listOf("request-0", "request-1", "request-2"),
            repository.receivedRequestIds,
        )
    }
}
