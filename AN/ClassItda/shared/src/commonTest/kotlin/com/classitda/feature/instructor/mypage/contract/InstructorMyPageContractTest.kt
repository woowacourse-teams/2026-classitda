package com.classitda.feature.instructor.mypage.contract

import com.classitda.domain.model.instructor.mypage.InstructorMemberId
import com.classitda.domain.model.instructor.mypage.InstructorStudioId
import com.classitda.domain.model.instructor.mypage.StudioAddress
import com.classitda.domain.model.instructor.mypage.StudioImageSelection
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertTrue

class InstructorMyPageContractTest {
    @Test
    fun myPagePrivacyPolicyActionIsASeparateNavigationContract() {
        val action: InstructorMyPageAction = InstructorMyPageAction.OpenPrivacyPolicy

        assertIs<InstructorMyPageAction.OpenPrivacyPolicy>(action)
    }

    @Test
    fun memberActionCarriesStableMemberId() {
        val id = InstructorMemberId("member-1")

        val action = MemberManagementAction.EditMember(id)

        assertEquals(id, action.memberId)
    }

    @Test
    fun memberSortActionCarriesSelectedOrder() {
        val action = MemberManagementAction.SortOrderChanged(MemberSortOption.NAME_ASC)

        assertEquals(MemberSortOption.NAME_ASC, action.sortOrder)
    }

    @Test
    fun studioActionsKeepStableStudioIdAndRemainDistinct() {
        val id = InstructorStudioId("studio-1")

        val edit = StudioManagementAction.EditStudio(id)
        val detail = StudioManagementAction.OpenStudioDetail(id)

        assertEquals(id, edit.studioId)
        assertEquals(id, detail.studioId)
        assertIs<StudioManagementAction.EditStudio>(edit)
        assertIs<StudioManagementAction.OpenStudioDetail>(detail)
    }

    @Test
    fun listStatesKeepEmptyAndSearchEmptyMutuallyExclusive() {
        val empty: MemberManagementUiState = MemberManagementUiState.Empty()
        val searchEmpty: MemberManagementUiState = MemberManagementUiState.SearchEmpty("missing")

        assertIs<MemberManagementUiState.Empty>(empty)
        assertIs<MemberManagementUiState.SearchEmpty>(searchEmpty)
    }

    @Test
    fun memberRegistrationStatesCarryDraftAcrossConfirmationAndFailure() {
        val draft = MemberInputUiModel(name = "Member", phoneNumber = "01012345678")
        val confirmation: MemberRegistrationUiState = MemberRegistrationUiState.Confirmation(draft)
        val submitting: MemberRegistrationUiState = MemberRegistrationUiState.Submitting(draft)
        val failure: MemberRegistrationUiState =
            MemberRegistrationUiState.Error(
                draft = draft,
                reason = MemberRegistrationUiError.NETWORK,
            )

        assertEquals(draft, assertIs<MemberRegistrationUiState.Confirmation>(confirmation).draft)
        assertEquals(draft, assertIs<MemberRegistrationUiState.Submitting>(submitting).draft)
        assertEquals(draft, assertIs<MemberRegistrationUiState.Error>(failure).draft)
    }

    @Test
    fun memberRegistrationValidationOnlyMarksFieldsAfterSubmitAttempt() {
        val emptyDraft = MemberInputUiModel()
        val validDraft = MemberInputUiModel(name = "Member", phoneNumber = "01012345678")

        assertEquals(
            setOf(MemberRegistrationField.NAME, MemberRegistrationField.PHONE_NUMBER),
            memberRegistrationFieldErrors(emptyDraft),
        )
        assertTrue(memberRegistrationFieldErrors(validDraft).isEmpty())
        assertTrue(validDraft.isMemberRegistrationValid())
        assertFalse(memberRegistrationFieldErrors(validDraft.copy(phoneNumber = "010-1234-5678x")).isEmpty())
        assertTrue(memberEditFieldErrors(validDraft.copy(phoneNumber = "010 1234 5678")).isEmpty())
    }

    @Test
    fun successStatesExposeStableRegistrationIds() {
        val memberId = InstructorMemberId("member-1")
        assertEquals(
            memberId,
            assertIs<MemberRegistrationAction.SuccessAcknowledged>(
                MemberRegistrationAction.SuccessAcknowledged(memberId),
            ).memberId,
        )

        assertEquals(
            memberId,
            assertIs<MemberRegistrationUiState.Success>(MemberRegistrationUiState.Success(memberId)).memberId,
        )
        assertIs<StudioRegistrationUiState.Success>(StudioRegistrationUiState.Success)
    }

    @Test
    fun studioRegistrationActionsExposeExternalSelectionBoundaries() {
        val image = StudioImageInputUiModel(StudioImageSelection.Remote("https://example.com/studio.jpg"))
        val selectedImage = StudioRegistrationAction.ImageSelected(image)
        val address =
            StudioRegistrationAction.AddressSelected(
                StudioAddress(
                    roadAddress = "Seoul",
                    detailAddress = "Jongno",
                ),
            )

        assertEquals(image, selectedImage.image)
        assertEquals("Seoul", address.address.roadAddress)
        assertEquals("Jongno", address.address.detailAddress)
    }

    @Test
    fun studioImageContractSupportsSelectionReplacementAndRemoval() {
        val first =
            StudioImageInputUiModel(
                StudioImageSelection.Local("handle-1", "preview-1", "image/jpeg", "one.jpg", 10),
            )
        val replacement =
            StudioImageInputUiModel(
                StudioImageSelection.Local("handle-2", "preview-2", "image/png", "two.png", 20),
            )
        val initial = StudioInputUiModel(image = first)

        assertEquals(first, initial.image)
        assertEquals(replacement, initial.copy(image = replacement).image)
        assertEquals(null, initial.copy(image = null).image)
        assertIs<StudioRegistrationAction.RemoveImage>(StudioRegistrationAction.RemoveImage)
    }

    @Test
    fun studioRegistrationValidationKeepsInitialFieldsNeutralUntilSubmit() {
        val emptyDraft = StudioInputUiModel()
        val validDraft =
            StudioInputUiModel(
                name = "Studio",
                address = StudioAddress(roadAddress = "Seoul"),
                phoneNumber = "0212345678",
                openingTime = "09:00",
                closingTime = "22:00",
            )
        val invalidFormatDraft =
            validDraft.copy(
                phoneNumber = "02-12",
                openingTime = "9:00",
                closingTime = "25:00",
            )

        assertEquals(
            setOf(
                StudioRegistrationField.NAME,
                StudioRegistrationField.ADDRESS,
                StudioRegistrationField.PHONE_NUMBER,
                StudioRegistrationField.OPENING_TIME,
                StudioRegistrationField.CLOSING_TIME,
            ),
            studioRegistrationFieldErrors(emptyDraft),
        )
        assertTrue(studioRegistrationFieldErrors(validDraft).isEmpty())
        assertTrue(validDraft.isStudioRegistrationValid())
        assertEquals(
            setOf(
                StudioRegistrationField.PHONE_NUMBER,
                StudioRegistrationField.OPENING_TIME,
                StudioRegistrationField.CLOSING_TIME,
            ),
            studioRegistrationFieldErrors(invalidFormatDraft),
        )
    }

    @Test
    fun memberContentKeepsRepositoryPageAndQueryTogether() {
        val page = MemberListUiModel(totalCount = 0, members = emptyList())
        val state = MemberManagementUiState.Content(page = page, query = "query")

        assertEquals(page, state.page)
        assertEquals("query", state.query)
    }

    @Test
    fun studioDetailStateKeepsStableIdAndDeleteInputSeparate() {
        val studio = InstructorStudioId("studio-1")
        val confirming = StudioDeleteState.Confirming(typedName = "다른 이름")
        val state =
            StudioDetailUiState.Content(
                studio =
                    StudioUiModel(
                        id = studio,
                        name = "클래스잇다 스튜디오",
                        address = StudioAddress(roadAddress = "서울"),
                    ),
                deleteState = confirming,
            )

        assertEquals(studio, state.studio.id)
        assertEquals("다른 이름", assertIs<StudioDeleteState.Confirming>(state.deleteState).typedName)
        assertEquals(
            "다른 이름",
            assertIs<StudioDetailAction.DeleteNameChanged>(
                StudioDetailAction.DeleteNameChanged("다른 이름"),
            ).name,
        )
    }

    @Test
    fun studioEditSuccessCarriesStableStudioId() {
        val id = InstructorStudioId("studio-1")

        assertEquals(
            id,
            assertIs<StudioEditUiState.Success>(StudioEditUiState.Success(id)).studioId,
        )
    }
}
