package com.classitda.domain.model.student.mypage

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class MemberProfileTest {
    @Test
    fun `회원 이름이 blank이면 프로필을 생성할 수 없다`() {
        listOf("", " \t\n").forEach { name ->
            assertFailsWith<IllegalArgumentException> {
                createProfile(name = name)
            }
        }
    }

    @Test
    fun `회원 이름과 연락처와 이메일은 원문을 보존한다`() {
        val rawName = "  김민지  "
        val rawPhoneNumber = "+82 10 1234 5678"
        val rawEmail = "Class12345+student@Example.COM"

        val profile =
            createProfile(
                name = rawName,
                phoneNumber = rawPhoneNumber,
                email = rawEmail,
            )

        assertEquals(rawName, profile.name)
        assertEquals(rawPhoneNumber, profile.phoneNumber)
        assertEquals(rawEmail, profile.email)
    }

    @Test
    fun `프로필 이미지가 없어도 프로필을 생성할 수 있다`() {
        createProfile(profileImageUrl = null)
    }

    private fun createProfile(
        name: String = "김민지",
        phoneNumber: String = "010-1234-5678",
        email: String = "class12345@gmail.com",
        profileImageUrl: String? = "https://example.com/profile.png",
    ): MemberProfile =
        MemberProfile(
            id = MemberId("member-1"),
            name = name,
            phoneNumber = phoneNumber,
            email = email,
            profileImageUrl = profileImageUrl,
        )
}
