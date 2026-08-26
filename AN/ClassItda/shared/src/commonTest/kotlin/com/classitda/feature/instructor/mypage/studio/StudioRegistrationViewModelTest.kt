package com.classitda.feature.instructor.mypage.studio

import com.classitda.domain.model.instructor.mypage.InstructorStudioId
import com.classitda.domain.model.instructor.mypage.ManagedStudio
import com.classitda.domain.model.instructor.mypage.StudioAddress
import com.classitda.domain.model.instructor.mypage.StudioImageMutation
import com.classitda.domain.model.instructor.mypage.StudioImageSelection
import com.classitda.domain.model.instructor.mypage.StudioRegistrationDraft
import com.classitda.domain.repository.instructor.mypage.InstructorMyPageResult
import com.classitda.domain.repository.instructor.mypage.InstructorStudioRepository
import com.classitda.domain.repository.instructor.mypage.StudioList
import com.classitda.feature.instructor.mypage.contract.StudioImageInputUiModel
import com.classitda.feature.instructor.mypage.contract.StudioImageUiError
import com.classitda.feature.instructor.mypage.contract.StudioRegistrationAction
import com.classitda.feature.instructor.mypage.contract.StudioRegistrationField
import com.classitda.feature.instructor.mypage.contract.StudioRegistrationUiState
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs

class StudioRegistrationViewModelTest {
    @Test
    fun `주소 선택은 다섯 값을 보존하고 기존 상세 주소를 초기화한다`() {
        val viewModel = StudioRegistrationViewModel(NoOpStudioRepository)
        val selectedAddress =
            StudioAddress(
                zoneCode = "13494",
                roadAddress = "경기 성남시 분당구 판교역로 166",
                jibunAddress = "경기 성남시 분당구 백현동 532",
                buildingName = "카카오 판교 아지트",
                detailAddress = "새 주소에서 넘어온 상세값",
            )

        viewModel.onAction(StudioRegistrationAction.DetailAddressChanged("기존 상세 주소"))
        viewModel.onAction(StudioRegistrationAction.AddressSelected(selectedAddress))

        val draft = assertIs<StudioRegistrationUiState.Editing>(viewModel.uiState.value).draft
        assertEquals(selectedAddress.zoneCode, draft.address.zoneCode)
        assertEquals(selectedAddress.roadAddress, draft.address.roadAddress)
        assertEquals(selectedAddress.jibunAddress, draft.address.jibunAddress)
        assertEquals(selectedAddress.buildingName, draft.address.buildingName)
        assertEquals("", draft.address.detailAddress)
    }

    @Test
    fun `단일 이미지는 선택 교체 제거 상태를 순서대로 반영한다`() {
        val viewModel = StudioRegistrationViewModel(NoOpStudioRepository)
        val first =
            StudioImageInputUiModel(
                StudioImageSelection.Local("handle-1", "preview-1", "image/jpeg", "one.jpg", 10),
            )
        val replacement =
            StudioImageInputUiModel(
                StudioImageSelection.Local("handle-2", "preview-2", "image/png", "two.png", 20),
            )

        viewModel.onAction(StudioRegistrationAction.ImageSelected(first))
        assertEquals(first, assertIs<StudioRegistrationUiState.Editing>(viewModel.uiState.value).draft.image)

        viewModel.onAction(StudioRegistrationAction.ImageSelected(replacement))
        assertEquals(replacement, assertIs<StudioRegistrationUiState.Editing>(viewModel.uiState.value).draft.image)

        viewModel.onAction(StudioRegistrationAction.RemoveImage)
        assertEquals(null, assertIs<StudioRegistrationUiState.Editing>(viewModel.uiState.value).draft.image)
    }

    @Test
    fun `새 Local 이미지는 생성 요청을 위해 이미지 필드 오류로 막지 않는다`() {
        val viewModel = StudioRegistrationViewModel(NoOpStudioRepository)
        viewModel.onAction(StudioRegistrationAction.NameChanged("시설"))
        viewModel.onAction(
            StudioRegistrationAction.AddressSelected(
                StudioAddress(roadAddress = "서울시 강남구 테헤란로 1"),
            ),
        )
        viewModel.onAction(StudioRegistrationAction.PhoneNumberChanged("02-1234-5678"))
        viewModel.onAction(
            StudioRegistrationAction.ImageSelected(
                StudioImageInputUiModel(
                    StudioImageSelection.Local("handle", "preview", "image/jpeg", "one.jpg", 10),
                ),
            ),
        )

        val state = assertIs<StudioRegistrationUiState.Editing>(viewModel.uiState.value)
        assertFalse(StudioRegistrationField.IMAGE in state.fieldErrors)
    }

    @Test
    fun `이미지 선택 오류는 기존 이미지를 유지하고 오류 상태만 기록한다`() {
        val viewModel = StudioRegistrationViewModel(NoOpStudioRepository)
        val existing =
            StudioImageInputUiModel(
                StudioImageSelection.Local("handle", "preview", "image/jpeg", "one.jpg", 10),
            )
        viewModel.onAction(StudioRegistrationAction.ImageSelected(existing))

        viewModel.onAction(
            StudioRegistrationAction.ImagePickerFailed(
                StudioImageUiError.PERMISSION_DENIED,
            ),
        )

        val state = assertIs<StudioRegistrationUiState.Editing>(viewModel.uiState.value)
        assertEquals(existing, state.draft.image)
        assertEquals(StudioImageUiError.PERMISSION_DENIED, state.imageError)
    }
}

private object NoOpStudioRepository : InstructorStudioRepository {
    override suspend fun getStudios(): InstructorMyPageResult<StudioList> = error("not used")

    override suspend fun getStudio(studioId: InstructorStudioId): InstructorMyPageResult<ManagedStudio> =
        error("not used")

    override suspend fun registerStudio(draft: StudioRegistrationDraft): InstructorMyPageResult<Unit> =
        error("not used")

    override suspend fun updateStudio(
        studioId: InstructorStudioId,
        original: ManagedStudio,
        draft: StudioRegistrationDraft,
        imageMutation: StudioImageMutation,
    ): InstructorMyPageResult<Unit> = error("not used")
}
