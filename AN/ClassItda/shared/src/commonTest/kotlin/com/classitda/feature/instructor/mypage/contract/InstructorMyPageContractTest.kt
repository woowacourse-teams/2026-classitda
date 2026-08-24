package com.classitda.feature.instructor.mypage.contract

import com.classitda.domain.model.instructor.mypage.FacilityRegistrationDraft
import com.classitda.domain.model.instructor.mypage.InstructorFacilityId
import com.classitda.domain.model.instructor.mypage.InstructorMemberId
import com.classitda.domain.model.instructor.mypage.MemberListPage
import com.classitda.domain.model.instructor.mypage.MemberRegistrationDraft
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

class InstructorMyPageContractTest {
    @Test
    fun memberActionCarriesStableMemberId() {
        val id = InstructorMemberId("member-1")

        val action = MemberManagementAction.OpenMember(id)

        assertEquals(id, action.memberId)
    }

    @Test
    fun facilityActionsKeepStableFacilityIdAndRemainDistinct() {
        val id = InstructorFacilityId("facility-1")

        val edit = FacilityManagementAction.EditFacility(id)
        val detail = FacilityManagementAction.OpenFacilityDetail(id)

        assertEquals(id, edit.facilityId)
        assertEquals(id, detail.facilityId)
        assertIs<FacilityManagementAction.EditFacility>(edit)
        assertIs<FacilityManagementAction.OpenFacilityDetail>(detail)
    }

    @Test
    fun listStatesKeepEmptyAndSearchEmptyMutuallyExclusive() {
        val empty: MemberManagementUiState = MemberManagementUiState.Empty
        val searchEmpty: MemberManagementUiState = MemberManagementUiState.SearchEmpty("missing")

        assertIs<MemberManagementUiState.Empty>(empty)
        assertIs<MemberManagementUiState.SearchEmpty>(searchEmpty)
    }

    @Test
    fun memberRegistrationStatesCarryDraftAcrossConfirmationAndFailure() {
        val draft = MemberRegistrationDraft(name = "Member", phoneNumber = "01012345678")
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
        val emptyDraft = MemberRegistrationDraft()
        val validDraft = MemberRegistrationDraft(name = "Member", phoneNumber = "01012345678")

        assertEquals(
            setOf(MemberRegistrationField.NAME, MemberRegistrationField.PHONE_NUMBER),
            memberRegistrationFieldErrors(emptyDraft),
        )
        assertTrue(memberRegistrationFieldErrors(validDraft).isEmpty())
        assertTrue(validDraft.isMemberRegistrationValid())
    }

    @Test
    fun successStatesExposeStableRegistrationIds() {
        val memberId = InstructorMemberId("member-1")
        val facilityId = InstructorFacilityId("facility-1")

        assertEquals(
            memberId,
            assertIs<MemberRegistrationUiState.Success>(MemberRegistrationUiState.Success(memberId)).memberId,
        )
        assertEquals(
            facilityId,
            assertIs<FacilityRegistrationUiState.Success>(FacilityRegistrationUiState.Success(facilityId))
                .facilityId,
        )
    }

    @Test
    fun facilityRegistrationActionsExposeExternalSelectionBoundaries() {
        val draft = FacilityRegistrationDraft()
        val images = FacilityRegistrationAction.ImagesSelected(draft.images)
        val address = FacilityRegistrationAction.AddressSelected("Seoul", "Jongno")

        assertEquals(draft.images, images.images)
        assertEquals("Seoul", address.address)
        assertEquals("Jongno", address.detailAddress)
    }

    @Test
    fun facilityRegistrationValidationKeepsInitialFieldsNeutralUntilSubmit() {
        val emptyDraft = FacilityRegistrationDraft()
        val validDraft =
            FacilityRegistrationDraft(
                name = "Facility",
                address = "Seoul",
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
                FacilityRegistrationField.NAME,
                FacilityRegistrationField.ADDRESS,
                FacilityRegistrationField.PHONE_NUMBER,
            ),
            facilityRegistrationFieldErrors(emptyDraft),
        )
        assertTrue(facilityRegistrationFieldErrors(validDraft).isEmpty())
        assertTrue(validDraft.isFacilityRegistrationValid())
        assertEquals(
            setOf(
                FacilityRegistrationField.PHONE_NUMBER,
                FacilityRegistrationField.OPENING_TIME,
                FacilityRegistrationField.CLOSING_TIME,
            ),
            facilityRegistrationFieldErrors(invalidFormatDraft),
        )
    }

    @Test
    fun memberContentKeepsRepositoryPageAndQueryTogether() {
        val page = MemberListPage(totalCount = 0, members = emptyList())
        val state = MemberManagementUiState.Content(page = page, query = "query")

        assertEquals(page, state.page)
        assertEquals("query", state.query)
    }

    @Test
    fun facilityDetailStateKeepsStableIdAndDeleteInputSeparate() {
        val facility = InstructorFacilityId("facility-1")
        val confirming = FacilityDeleteState.Confirming(typedName = "다른 이름")
        val state =
            FacilityDetailUiState.Content(
                facility =
                    com.classitda.domain.model.instructor.mypage.ManagedFacility(
                        id = facility,
                        name = "클래스잇다 스튜디오",
                        address = "서울",
                    ),
                deleteState = confirming,
            )

        assertEquals(facility, state.facility.id)
        assertEquals("다른 이름", assertIs<FacilityDeleteState.Confirming>(state.deleteState).typedName)
        assertEquals(
            "다른 이름",
            assertIs<FacilityDetailAction.DeleteNameChanged>(
                FacilityDetailAction.DeleteNameChanged("다른 이름"),
            ).name,
        )
    }

    @Test
    fun facilityEditSuccessCarriesStableFacilityId() {
        val id = InstructorFacilityId("facility-1")

        assertEquals(
            id,
            assertIs<FacilityEditUiState.Success>(FacilityEditUiState.Success(id)).facilityId,
        )
    }
}
