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
            ::InstructorStudioId,
        ).forEach { createId ->
            assertFailsWith<IllegalArgumentException> { createId("") }
            assertFailsWith<IllegalArgumentException> { createId(" \t\n") }
        }

        assertFailsWith<IllegalArgumentException> {
            StudioImageSelection.Local(
                handle = "",
                previewReference = "opaque-reference",
                mimeType = "image/jpeg",
                fileName = "studio.jpg",
                sizeBytes = 1,
            )
        }
    }

    @Test
    fun `강사 회원 시설 인증 ID는 같은 원문이어도 서로 다른 타입이다`() {
        val rawValue = "same-id"

        assertNotEquals<Any>(InstructorMemberId(rawValue), InstructorStudioId(rawValue))
        assertNotEquals<Any>(InstructorMemberId(rawValue), InstructorPhoneVerificationId(rawValue))
        assertNotEquals<Any>(InstructorStudioId(rawValue), InstructorPhoneVerificationId(rawValue))
    }

    @Test
    fun `도메인 모델은 이름 전화번호 주소와 이미지 참조의 원문을 보존한다`() {
        val profile =
            InstructorAccountProfile(
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
        val studio =
            ManagedStudio(
                id = InstructorStudioId("studio-1"),
                name = "  Studio / 강남  ",
                address =
                    StudioAddress(
                        roadAddress = "서울시",
                        detailAddress = "상세 주소",
                    ),
            )

        assertEquals("  강사 / Studio  ", profile.name)
        assertEquals("+821012345678", profile.phoneNumber)
        assertEquals("  회원  ", member.name)
        assertEquals("01012345678", member.phoneNumber)
        assertEquals("서울시", studio.address.displayAddress)
        assertEquals("상세 주소", studio.address.detailAddress)
    }

    @Test
    fun `시설 주소는 다섯 필드를 보존하고 도로명 없을 때 지번을 표시한다`() {
        val address =
            StudioAddress(
                zoneCode = "13494",
                roadAddress = "경기 성남시 분당구 판교역로 166",
                jibunAddress = "경기 성남시 분당구 백현동 532",
                buildingName = "카카오 판교 아지트",
                detailAddress = "3층",
            )

        assertEquals("13494", address.zoneCode)
        assertEquals("경기 성남시 분당구 판교역로 166", address.roadAddress)
        assertEquals("경기 성남시 분당구 백현동 532", address.jibunAddress)
        assertEquals("카카오 판교 아지트", address.buildingName)
        assertEquals("3층", address.detailAddress)
        assertEquals(address.roadAddress, address.displayAddress)
        assertEquals(address.jibunAddress, address.copy(roadAddress = "").displayAddress)
    }

    @Test
    fun `시설 등록 draft는 단일 이미지를 보유한다`() {
        val image = StudioImageSelection.Remote("https://example.com/studio.jpg")
        val draft = StudioRegistrationDraft(image = image)

        assertEquals(image, draft.image)
    }

    @Test
    fun `회원 목록 총원은 음수일 수 없다`() {
        assertFailsWith<IllegalArgumentException> {
            MemberListPage(totalCount = -1, members = emptyList())
        }
    }
}
