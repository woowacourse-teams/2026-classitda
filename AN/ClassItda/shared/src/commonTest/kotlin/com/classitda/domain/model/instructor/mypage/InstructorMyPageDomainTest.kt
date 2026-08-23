package com.classitda.domain.model.instructor.mypage

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotEquals

class InstructorMyPageDomainTest {
    @Test
    fun `강사 마이페이지 ID는 빈 문자열과 공백 문자열을 거부한다`() {
        listOf<(String) -> Any>(
            ::InstructorPhoneVerificationId,
            ::InstructorMemberId,
            ::InstructorFacilityId,
        ).forEach { createId ->
            assertFailsWith<IllegalArgumentException> { createId("") }
            assertFailsWith<IllegalArgumentException> { createId(" \t\n") }
        }

        assertFailsWith<IllegalArgumentException> {
            InstructorAccountProfile(
                id = "",
                name = "강사",
                phoneNumber = "01012345678",
                email = "instructor@example.com",
                profileImageUrl = null,
            )
        }

        assertFailsWith<IllegalArgumentException> {
            FacilityImageDraft(id = "", previewReference = "opaque-reference")
        }
    }

    @Test
    fun `강사 회원 시설 인증 ID는 같은 원문이어도 서로 다른 타입이다`() {
        val rawValue = "same-id"

        assertNotEquals<Any>(InstructorMemberId(rawValue), InstructorFacilityId(rawValue))
        assertNotEquals<Any>(InstructorMemberId(rawValue), InstructorPhoneVerificationId(rawValue))
        assertNotEquals<Any>(InstructorFacilityId(rawValue), InstructorPhoneVerificationId(rawValue))
    }

    @Test
    fun `도메인 모델은 이름 전화번호 주소와 이미지 참조의 원문을 보존한다`() {
        val profile =
            InstructorAccountProfile(
                id = "account-1",
                name = "  강사 / Studio  ",
                phoneNumber = "+821012345678",
                email = "Instructor@Example.COM",
                profileImageUrl = "https://example.com/avatar.png",
            )
        val member =
            ManagedMember(
                id = InstructorMemberId("member-1"),
                name = "  회원  ",
                phoneNumber = "01012345678",
            )
        val facility =
            ManagedFacility(
                id = InstructorFacilityId("facility-1"),
                name = "  Studio / 강남  ",
                address = "서울시 / 상세 주소",
            )

        assertEquals("  강사 / Studio  ", profile.name)
        assertEquals("+821012345678", profile.phoneNumber)
        assertEquals("  회원  ", member.name)
        assertEquals("01012345678", member.phoneNumber)
        assertEquals("서울시 / 상세 주소", facility.address)
    }

    @Test
    fun `시설 등록 draft는 이미지 다섯 장까지 보유할 수 있다`() {
        val images =
            (1..FacilityRegistrationDraft.MAX_IMAGE_COUNT).map { index ->
                FacilityImageDraft(
                    id = "image-$index",
                    previewReference = "opaque-reference-$index",
                )
            }

        val draft = FacilityRegistrationDraft(images = images)

        assertEquals(FacilityRegistrationDraft.MAX_IMAGE_COUNT, draft.images.size)
    }

    @Test
    fun `시설 등록 draft는 이미지가 여섯 장이면 생성할 수 없다`() {
        val images =
            (1..FacilityRegistrationDraft.MAX_IMAGE_COUNT + 1).map { index ->
                FacilityImageDraft(
                    id = "image-$index",
                    previewReference = "opaque-reference-$index",
                )
            }

        assertFailsWith<IllegalArgumentException> {
            FacilityRegistrationDraft(images = images)
        }
    }

    @Test
    fun `회원 목록 총원은 음수일 수 없다`() {
        assertFailsWith<IllegalArgumentException> {
            MemberListPage(totalCount = -1, members = emptyList())
        }
    }
}
