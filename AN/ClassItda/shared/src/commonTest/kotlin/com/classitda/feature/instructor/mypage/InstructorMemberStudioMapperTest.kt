package com.classitda.feature.instructor.mypage

import com.classitda.domain.model.instructor.mypage.InstructorMemberId
import com.classitda.domain.model.instructor.mypage.InstructorStudioId
import com.classitda.domain.model.instructor.mypage.ManagedMember
import com.classitda.domain.model.instructor.mypage.ManagedStudio
import com.classitda.domain.model.instructor.mypage.StudioAddress
import com.classitda.domain.model.instructor.mypage.StudioImageSelection
import com.classitda.feature.instructor.mypage.contract.MemberInputUiModel
import com.classitda.feature.instructor.mypage.contract.StudioImageInputUiModel
import com.classitda.feature.instructor.mypage.contract.StudioInputUiModel
import kotlin.test.Test
import kotlin.test.assertEquals

class InstructorMemberStudioMapperTest {
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
    fun managedStudioBecomesDisplayModelWithFormattedPhoneAndImages() {
        val uiModel =
            ManagedStudio(
                id = InstructorStudioId("studio-1"),
                name = "클래스잇다 스튜디오",
                address = StudioAddress(roadAddress = "서울 강남구 테헤란로 123"),
                phoneNumber = "0212345678",
                image = StudioImageSelection.Remote("preview-1"),
            ).toStudioUiModel()

        assertEquals("02-1234-5678", uiModel.phoneNumber)
        assertEquals("preview-1", uiModel.image?.previewReference)
    }

    @Test
    fun studioInputModelBecomesRepositoryDraftAndKeepsImageBoundaries() {
        val draft =
            StudioInputUiModel(
                image = StudioImageInputUiModel(StudioImageSelection.Remote("preview-1")),
                name = "클래스잇다 스튜디오",
                address = StudioAddress(roadAddress = "서울 강남구 테헤란로 123"),
                phoneNumber = "0212345678",
            ).toStudioRegistrationDraft()

        assertEquals("클래스잇다 스튜디오", draft.name)
        assertEquals("0212345678", draft.phoneNumber)
        assertEquals("preview-1", draft.image?.previewReference)
    }
}
