package com.classitda.feature.instructor.mypage.facility

import com.classitda.domain.model.instructor.mypage.FacilityAddress
import com.classitda.domain.model.instructor.mypage.FacilityImageSelection
import com.classitda.domain.model.instructor.mypage.FacilityRegistrationDraft
import com.classitda.domain.model.instructor.mypage.InstructorFacilityId
import com.classitda.domain.model.instructor.mypage.ManagedFacility
import com.classitda.domain.repository.instructor.mypage.FacilityList
import com.classitda.domain.repository.instructor.mypage.InstructorFacilityRepository
import com.classitda.domain.repository.instructor.mypage.InstructorMyPageResult
import com.classitda.feature.instructor.mypage.contract.FacilityRegistrationAction
import com.classitda.feature.instructor.mypage.contract.FacilityRegistrationUiState
import com.classitda.feature.instructor.mypage.contract.FacilityImageInputUiModel
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class FacilityRegistrationViewModelTest {
    @Test
    fun `주소 선택은 다섯 값을 보존하고 기존 상세 주소를 초기화한다`() {
        val viewModel = FacilityRegistrationViewModel(NoOpFacilityRepository)
        val selectedAddress =
            FacilityAddress(
                zoneCode = "13494",
                roadAddress = "경기 성남시 분당구 판교역로 166",
                jibunAddress = "경기 성남시 분당구 백현동 532",
                buildingName = "카카오 판교 아지트",
                detailAddress = "새 주소에서 넘어온 상세값",
            )

        viewModel.onAction(FacilityRegistrationAction.DetailAddressChanged("기존 상세 주소"))
        viewModel.onAction(FacilityRegistrationAction.AddressSelected(selectedAddress))

        val draft = assertIs<FacilityRegistrationUiState.Editing>(viewModel.uiState.value).draft
        assertEquals(selectedAddress.zoneCode, draft.address.zoneCode)
        assertEquals(selectedAddress.roadAddress, draft.address.roadAddress)
        assertEquals(selectedAddress.jibunAddress, draft.address.jibunAddress)
        assertEquals(selectedAddress.buildingName, draft.address.buildingName)
        assertEquals("", draft.address.detailAddress)
    }

    @Test
    fun `단일 이미지는 선택 교체 제거 상태를 순서대로 반영한다`() {
        val viewModel = FacilityRegistrationViewModel(NoOpFacilityRepository)
        val first =
            FacilityImageInputUiModel(
                FacilityImageSelection.Local("handle-1", "preview-1", "image/jpeg", "one.jpg", 10),
            )
        val replacement =
            FacilityImageInputUiModel(
                FacilityImageSelection.Local("handle-2", "preview-2", "image/png", "two.png", 20),
            )

        viewModel.onAction(FacilityRegistrationAction.ImageSelected(first))
        assertEquals(first, assertIs<FacilityRegistrationUiState.Editing>(viewModel.uiState.value).draft.image)

        viewModel.onAction(FacilityRegistrationAction.ImageSelected(replacement))
        assertEquals(replacement, assertIs<FacilityRegistrationUiState.Editing>(viewModel.uiState.value).draft.image)

        viewModel.onAction(FacilityRegistrationAction.RemoveImage)
        assertEquals(null, assertIs<FacilityRegistrationUiState.Editing>(viewModel.uiState.value).draft.image)
    }
}

private object NoOpFacilityRepository : InstructorFacilityRepository {
    override suspend fun getFacilities(): InstructorMyPageResult<FacilityList> = error("not used")

    override suspend fun getFacility(facilityId: InstructorFacilityId): InstructorMyPageResult<ManagedFacility> =
        error("not used")

    override suspend fun registerFacility(draft: FacilityRegistrationDraft): InstructorMyPageResult<Unit> =
        error("not used")

    override suspend fun updateFacility(
        facilityId: InstructorFacilityId,
        draft: FacilityRegistrationDraft,
    ): InstructorMyPageResult<Unit> = error("not used")
}
