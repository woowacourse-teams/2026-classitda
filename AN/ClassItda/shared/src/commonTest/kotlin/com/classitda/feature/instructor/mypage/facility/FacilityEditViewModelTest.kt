package com.classitda.feature.instructor.mypage.facility

import com.classitda.domain.model.instructor.mypage.FacilityAddress
import com.classitda.domain.model.instructor.mypage.FacilityImageMutation
import com.classitda.domain.model.instructor.mypage.FacilityImageSelection
import com.classitda.domain.model.instructor.mypage.FacilityRegistrationDraft
import com.classitda.domain.model.instructor.mypage.InstructorFacilityId
import com.classitda.domain.model.instructor.mypage.ManagedFacility
import com.classitda.domain.repository.instructor.mypage.FacilityList
import com.classitda.domain.repository.instructor.mypage.FacilityUpdateOperation
import com.classitda.domain.repository.instructor.mypage.InstructorFacilityRepository
import com.classitda.domain.repository.instructor.mypage.InstructorMyPageFailureReason
import com.classitda.domain.repository.instructor.mypage.InstructorMyPageResult
import com.classitda.feature.instructor.mypage.contract.FacilityEditAction
import com.classitda.feature.instructor.mypage.contract.FacilityEditUiState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import kotlinx.coroutines.withTimeout
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class FacilityEditViewModelTest {
    @BeforeTest
    fun setUpMainDispatcher() {
        Dispatchers.setMain(Dispatchers.Unconfined)
    }

    @AfterTest
    fun resetMainDispatcher() {
        Dispatchers.resetMain()
    }

    @Test
    fun `수정 제출은 원본과 draft를 비교하고 변경 이미지 mutation을 전달한다`() =
        runBlocking {
            val repository = RecordingEditRepository()
            val viewModel = FacilityEditViewModel(repository, FACILITY_ID)

            viewModel.uiState.awaitState<FacilityEditUiState.Editing>()
            viewModel.onAction(FacilityEditAction.NameChanged("변경 시설"))
            viewModel.onAction(FacilityEditAction.Submit)

            assertIs<FacilityEditUiState.Success>(viewModel.uiState.value)
            assertEquals("변경 시설", repository.lastDraft?.name)
            assertEquals(FacilityImageMutation.Unchanged, repository.lastImageMutation)
            assertEquals(FACILITY_ID, repository.lastFacilityId)
        }

    @Test
    fun `기존 Remote 이미지 제거는 Remove mutation으로 전달한다`() =
        runBlocking {
            val repository = RecordingEditRepository()
            val viewModel = FacilityEditViewModel(repository, FACILITY_ID)

            viewModel.uiState.awaitState<FacilityEditUiState.Editing>()
            viewModel.onAction(FacilityEditAction.RemoveImage)
            viewModel.onAction(FacilityEditAction.Submit)

            assertIs<FacilityEditUiState.Success>(viewModel.uiState.value)
            assertEquals(FacilityImageMutation.Remove, repository.lastImageMutation)
        }

    @Test
    fun `부분 성공 오류를 보존하고 재시도 시 성공 상태로 전이한다`() =
        runBlocking {
            val repository =
                RecordingEditRepository(
                    updateResults =
                        ArrayDeque(
                            listOf(
                                InstructorMyPageResult.Failure(
                                    InstructorMyPageFailureReason.SERVER,
                                    setOf(FacilityUpdateOperation.PATCH),
                                ),
                                InstructorMyPageResult.Success(Unit),
                            ),
                        ),
                )
            val viewModel = FacilityEditViewModel(repository, FACILITY_ID)

            viewModel.uiState.awaitState<FacilityEditUiState.Editing>()
            viewModel.onAction(FacilityEditAction.RemoveImage)
            viewModel.onAction(FacilityEditAction.Submit)

            val failure = assertIs<FacilityEditUiState.Error>(viewModel.uiState.value)
            assertEquals(setOf(FacilityUpdateOperation.PATCH), failure.completedOperations)

            viewModel.onAction(FacilityEditAction.Retry)

            assertIs<FacilityEditUiState.Success>(viewModel.uiState.value)
            assertEquals(2, repository.updateCalls)
        }

    companion object {
        val FACILITY_ID = InstructorFacilityId("42")
        val REMOTE_IMAGE = FacilityImageSelection.Remote("https://cdn.classitda.com/studio.jpg")
        val FACILITY =
            ManagedFacility(
                id = FACILITY_ID,
                name = "클래스잇다 스튜디오",
                address = FacilityAddress(zoneCode = "13494", roadAddress = "경기 성남시 분당구 판교역로 166"),
                image = REMOTE_IMAGE,
                phoneNumber = "031-123-4567",
                description = "시설 설명",
                openingTime = "09:00",
                closingTime = "22:00",
            )
    }
}

private class RecordingEditRepository(
    private val updateResults: ArrayDeque<InstructorMyPageResult<Unit>> =
        ArrayDeque(listOf(InstructorMyPageResult.Success(Unit))),
) : InstructorFacilityRepository {
    var lastFacilityId: InstructorFacilityId? = null
        private set
    var lastDraft: FacilityRegistrationDraft? = null
        private set
    var lastImageMutation: FacilityImageMutation? = null
        private set
    var updateCalls: Int = 0
        private set

    override suspend fun getFacilities(): InstructorMyPageResult<FacilityList> = error("not used")

    override suspend fun getFacility(facilityId: InstructorFacilityId): InstructorMyPageResult<ManagedFacility> =
        InstructorMyPageResult.Success(FacilityEditViewModelTest.FACILITY)

    override suspend fun registerFacility(draft: FacilityRegistrationDraft): InstructorMyPageResult<Unit> =
        error("not used")

    override suspend fun updateFacility(
        facilityId: InstructorFacilityId,
        original: ManagedFacility,
        draft: FacilityRegistrationDraft,
        imageMutation: FacilityImageMutation,
    ): InstructorMyPageResult<Unit> {
        updateCalls += 1
        lastFacilityId = facilityId
        lastDraft = draft
        lastImageMutation = imageMutation
        return updateResults.removeFirst()
    }
}

private suspend inline fun <reified T> kotlinx.coroutines.flow.StateFlow<*>.awaitState(): T =
    withTimeout(5_000) {
        first { it is T } as T
    }
