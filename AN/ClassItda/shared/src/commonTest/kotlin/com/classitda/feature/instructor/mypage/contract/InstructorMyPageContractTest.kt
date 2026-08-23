package com.classitda.feature.instructor.mypage.contract

import com.classitda.domain.model.instructor.mypage.FacilityRegistrationDraft
import com.classitda.domain.model.instructor.mypage.InstructorFacilityId
import com.classitda.domain.model.instructor.mypage.InstructorMemberId
import com.classitda.domain.model.instructor.mypage.MemberListPage
import com.classitda.domain.model.instructor.mypage.MemberRegistrationDraft
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

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
        val failure: MemberRegistrationUiState =
            MemberRegistrationUiState.Error(
                draft = draft,
                reason = MemberRegistrationUiError.NETWORK,
            )

        assertEquals(draft, assertIs<MemberRegistrationUiState.Confirmation>(confirmation).draft)
        assertEquals(draft, assertIs<MemberRegistrationUiState.Error>(failure).draft)
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
    fun memberContentKeepsRepositoryPageAndQueryTogether() {
        val page = MemberListPage(totalCount = 0, members = emptyList())
        val state = MemberManagementUiState.Content(page = page, query = "query")

        assertEquals(page, state.page)
        assertEquals("query", state.query)
    }
}
