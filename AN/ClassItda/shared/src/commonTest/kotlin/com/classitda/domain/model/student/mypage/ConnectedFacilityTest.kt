package com.classitda.domain.model.student.mypage

import kotlinx.datetime.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class ConnectedFacilityTest {
    @Test
    fun `시설 이름이 blank이면 연결 시설을 생성할 수 없다`() {
        listOf("", " \t\n").forEach { name ->
            assertFailsWith<IllegalArgumentException> {
                createFacility(name = name)
            }
        }
    }

    @Test
    fun `시설 이름은 원문을 보존한다`() {
        val rawName = "  필라테스 더 밸런스 강남점  "

        assertEquals(rawName, createFacility(name = rawName).name)
    }

    private fun createFacility(name: String): ConnectedFacility =
        ConnectedFacility(
            id = FacilityId("facility-1"),
            name = name,
            connectedOn = LocalDate(2023, 12, 1),
        )
}
