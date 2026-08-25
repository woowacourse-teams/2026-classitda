package com.classitda.feature.instructor.mypage.facility

import com.classitda.domain.model.instructor.mypage.FacilityAddress
import com.classitda.domain.model.instructor.mypage.FacilityImageMutation
import com.classitda.domain.model.instructor.mypage.FacilityImageSelection
import com.classitda.domain.model.instructor.mypage.FacilityRegistrationDraft
import com.classitda.domain.model.instructor.mypage.InstructorFacilityId
import com.classitda.domain.model.instructor.mypage.ManagedFacility
import com.classitda.domain.repository.instructor.mypage.FacilityList
import com.classitda.domain.repository.instructor.mypage.InstructorFacilityRepository
import com.classitda.domain.repository.instructor.mypage.InstructorMyPageResult
import com.classitda.feature.instructor.mypage.contract.FacilityImageInputUiModel
import com.classitda.feature.instructor.mypage.contract.FacilityImageUiError
import com.classitda.feature.instructor.mypage.contract.FacilityRegistrationAction
import com.classitda.feature.instructor.mypage.contract.FacilityRegistrationField
import com.classitda.feature.instructor.mypage.contract.FacilityRegistrationUiState
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
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

    @Test
    fun `새 Local 이미지는 생성 요청을 위해 이미지 필드 오류로 막지 않는다`() {
        val viewModel = FacilityRegistrationViewModel(NoOpFacilityRepository)
        viewModel.onAction(FacilityRegistrationAction.NameChanged("시설"))
        viewModel.onAction(
            FacilityRegistrationAction.AddressSelected(
                FacilityAddress(roadAddress = "서울시 강남구 테헤란로 1"),
            ),
        )
        viewModel.onAction(FacilityRegistrationAction.PhoneNumberChanged("02-1234-5678"))
        viewModel.onAction(
            FacilityRegistrationAction.ImageSelected(
                FacilityImageInputUiModel(
                    FacilityImageSelection.Local("handle", "preview", "image/jpeg", "one.jpg", 10),
                ),
            ),
        )

        val state = assertIs<FacilityRegistrationUiState.Editing>(viewModel.uiState.value)
        assertFalse(FacilityRegistrationField.IMAGE in state.fieldErrors)
    }

    @Test
    fun `이미지 선택 오류는 기존 이미지를 유지하고 오류 상태만 기록한다`() {
        val viewModel = FacilityRegistrationViewModel(NoOpFacilityRepository)
        val existing =
            FacilityImageInputUiModel(
                FacilityImageSelection.Local("handle", "preview", "image/jpeg", "one.jpg", 10),
            )
        viewModel.onAction(FacilityRegistrationAction.ImageSelected(existing))

        viewModel.onAction(
            FacilityRegistrationAction.ImagePickerFailed(
                FacilityImageUiError.PERMISSION_DENIED,
            ),
        )

        val state = assertIs<FacilityRegistrationUiState.Editing>(viewModel.uiState.value)
        assertEquals(existing, state.draft.image)
        assertEquals(FacilityImageUiError.PERMISSION_DENIED, state.imageError)
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
        original: ManagedFacility,
        draft: FacilityRegistrationDraft,
        imageMutation: FacilityImageMutation,
    ): InstructorMyPageResult<Unit> = error("not used")
}
