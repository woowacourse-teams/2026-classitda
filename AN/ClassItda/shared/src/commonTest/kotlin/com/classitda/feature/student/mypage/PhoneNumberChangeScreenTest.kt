package com.classitda.feature.student.mypage

import kotlin.test.Test
import kotlin.test.assertEquals

class PhoneNumberChangeScreenTest {
    @Test
    fun `인증번호 입력은 숫자 여섯 자리만 보존한다`() {
        assertEquals("123456", sanitizeVerificationCode("12a345678"))
    }

    @Test
    fun `남은 시간은 고정된 분 초 문자열로 표시한다`() {
        assertEquals("03:00", formatRemainingTime(180))
        assertEquals("00:00", formatRemainingTime(0))
        assertEquals("00:00", formatRemainingTime(-1))
    }
}
