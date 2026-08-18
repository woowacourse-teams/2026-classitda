package com.classitda.domain.model.student.myschedule

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class MyScheduleIdTest {
    private val idFactories: List<(String) -> Any> =
        listOf(
            ::ClassSessionId,
            ::ReservationId,
            ::WaitlistId,
            ::InstructorId,
            ::FacilityId,
            ::MemberPassId,
        )

    @Test
    fun `모든 ID는 빈 문자열과 공백 문자열을 거부한다`() {
        idFactories.forEach { createId ->
            assertFailsWith<IllegalArgumentException> { createId("") }
            assertFailsWith<IllegalArgumentException> { createId(" \t\n") }
        }
    }

    @Test
    fun `모든 ID는 서버 원문의 앞뒤 공백을 보존한다`() {
        val rawValue = "  server-id  "

        val values =
            listOf(
                ClassSessionId(rawValue).value,
                ReservationId(rawValue).value,
                WaitlistId(rawValue).value,
                InstructorId(rawValue).value,
                FacilityId(rawValue).value,
                MemberPassId(rawValue).value,
            )

        values.forEach { value -> assertEquals(rawValue, value) }
    }
}
