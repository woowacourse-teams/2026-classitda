package com.classitda.domain.model.student.mypage

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotEquals

class MyPageIdTest {
    private val idFactories: List<(String) -> Any> =
        listOf(
            ::MemberId,
            ::FacilityId,
        )

    @Test
    fun `모든 ID는 빈 문자열과 공백 문자열을 거부한다`() {
        idFactories.forEach { createId ->
            assertFailsWith<IllegalArgumentException> { createId("") }
            assertFailsWith<IllegalArgumentException> { createId(" \t\n") }
        }
    }

    @Test
    fun `모든 ID는 원문의 앞뒤 공백을 보존한다`() {
        val rawValue = "  server-id  "

        assertEquals(rawValue, MemberId(rawValue).value)
        assertEquals(rawValue, FacilityId(rawValue).value)
    }

    @Test
    fun `같은 원문을 가진 회원 ID와 시설 ID는 서로 다른 타입이다`() {
        val rawValue = "same-id"

        assertNotEquals<Any>(MemberId(rawValue), FacilityId(rawValue))
    }
}
