package com.classitda.domain.model.auth.signup

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class SignupTest {
    @Test
    fun `회원가입 입력값은 서버 계약 형식을 검증한다`() {
        assertEquals("클래스잇다", SignupName("클래스잇다").value)
        assertEquals("01012345678", SignupPhoneNumber("01012345678").value)
        assertEquals("123456", PhoneVerificationCode("123456").value)
    }

    @Test
    fun `잘못된 휴대전화번호와 인증번호는 생성할 수 없다`() {
        assertFailsWith<IllegalArgumentException> { SignupPhoneNumber("010-1234-5678") }
        assertFailsWith<IllegalArgumentException> { PhoneVerificationCode("12345") }
    }

    @Test
    fun `이름은 비어 있거나 50자를 넘을 수 없다`() {
        assertFailsWith<IllegalArgumentException> { SignupName(" ") }
        assertFailsWith<IllegalArgumentException> { SignupName("가".repeat(51)) }
    }
}
