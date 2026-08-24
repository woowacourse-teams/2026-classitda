package com.classitda.feature.instructor.mypage

import com.classitda.domain.model.instructor.mypage.FacilityImageDraft
import com.classitda.domain.model.instructor.mypage.InstructorFacilityId
import com.classitda.domain.model.instructor.mypage.InstructorMemberId
import com.classitda.domain.model.instructor.mypage.ManagedFacility
import com.classitda.domain.model.instructor.mypage.ManagedMember
import com.classitda.feature.instructor.mypage.contract.FacilityImageInputUiModel
import com.classitda.feature.instructor.mypage.contract.FacilityInputUiModel
import com.classitda.feature.instructor.mypage.contract.MemberInputUiModel
import kotlin.test.Test
import kotlin.test.assertEquals

class InstructorMemberFacilityMapperTest {
    @Test
    fun managedMemberBecomesDisplayModelWithMaskedPhoneAndFallback() {
        val uiModel =
            ManagedMember(
                id = InstructorMemberId("member-1"),
                name = "김민지",
                phoneNumber = "01012345678",
            ).toMemberUiModel()

        assertEquals("010-****-5678", uiModel.phoneNumber)
        assertEquals("김", uiModel.avatarFallback)
        assertEquals("member-1", uiModel.id.value)
    }

    @Test
    fun memberInputModelBecomesRepositoryDraftWithoutDisplayFormatting() {
        val draft = MemberInputUiModel(name = "김민지", phoneNumber = "01012345678").toMemberRegistrationDraft()

        assertEquals("김민지", draft.name)
        assertEquals("01012345678", draft.phoneNumber)
    }

    @Test
    fun managedFacilityBecomesDisplayModelWithFormattedPhoneAndImages() {
        val uiModel =
            ManagedFacility(
                id = InstructorFacilityId("facility-1"),
                name = "클래스잇다 스튜디오",
                address = "서울 강남구 테헤란로 123",
                phoneNumber = "0212345678",
                images = listOf(FacilityImageDraft("image-1", "preview-1")),
            ).toFacilityUiModel()

        assertEquals("02-1234-5678", uiModel.phoneNumber)
        assertEquals("preview-1", uiModel.images.single().previewReference)
    }

    @Test
    fun facilityInputModelBecomesRepositoryDraftAndKeepsImageBoundaries() {
        val draft =
            FacilityInputUiModel(
                images = listOf(FacilityImageInputUiModel("image-1", "preview-1")),
                name = "클래스잇다 스튜디오",
                address = "서울 강남구 테헤란로 123",
                phoneNumber = "0212345678",
            ).toFacilityRegistrationDraft()

        assertEquals("클래스잇다 스튜디오", draft.name)
        assertEquals("0212345678", draft.phoneNumber)
        assertEquals("image-1", draft.images.single().id)
    }
}
