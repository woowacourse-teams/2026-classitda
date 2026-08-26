package com.classitda.feature.instructor.mypage.studio

import com.classitda.domain.model.instructor.mypage.InstructorStudioId
import com.classitda.domain.model.instructor.mypage.ManagedStudio
import com.classitda.domain.model.instructor.mypage.StudioAddress
import com.classitda.domain.model.instructor.mypage.StudioImageMutation
import com.classitda.domain.model.instructor.mypage.StudioImageSelection
import com.classitda.domain.model.instructor.mypage.StudioRegistrationDraft
import com.classitda.domain.repository.instructor.mypage.InstructorMyPageFailureReason
import com.classitda.domain.repository.instructor.mypage.InstructorMyPageResult
import com.classitda.domain.repository.instructor.mypage.InstructorStudioRepository
import com.classitda.domain.repository.instructor.mypage.StudioList
import com.classitda.domain.repository.instructor.mypage.StudioUpdateOperation
import com.classitda.feature.instructor.mypage.contract.StudioEditAction
import com.classitda.feature.instructor.mypage.contract.StudioEditUiState
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

class StudioEditViewModelTest {
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
            val viewModel = StudioEditViewModel(repository, STUDIO_ID)

            viewModel.uiState.awaitState<StudioEditUiState.Editing>()
            viewModel.onAction(StudioEditAction.NameChanged("변경 시설"))
            viewModel.onAction(StudioEditAction.Submit)

            assertIs<StudioEditUiState.Success>(viewModel.uiState.value)
            assertEquals("변경 시설", repository.lastDraft?.name)
            assertEquals(StudioImageMutation.Unchanged, repository.lastImageMutation)
            assertEquals(STUDIO_ID, repository.lastStudioId)
        }

    @Test
    fun `기존 Remote 이미지 제거는 Remove mutation으로 전달한다`() =
        runBlocking {
            val repository = RecordingEditRepository()
            val viewModel = StudioEditViewModel(repository, STUDIO_ID)

            viewModel.uiState.awaitState<StudioEditUiState.Editing>()
            viewModel.onAction(StudioEditAction.RemoveImage)
            viewModel.onAction(StudioEditAction.Submit)

            assertIs<StudioEditUiState.Success>(viewModel.uiState.value)
            assertEquals(StudioImageMutation.Remove, repository.lastImageMutation)
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
                                    setOf(StudioUpdateOperation.PATCH),
                                ),
                                InstructorMyPageResult.Success(Unit),
                            ),
                        ),
                )
            val viewModel = StudioEditViewModel(repository, STUDIO_ID)

            viewModel.uiState.awaitState<StudioEditUiState.Editing>()
            viewModel.onAction(StudioEditAction.RemoveImage)
            viewModel.onAction(StudioEditAction.Submit)

            val failure = assertIs<StudioEditUiState.Error>(viewModel.uiState.value)
            assertEquals(setOf(StudioUpdateOperation.PATCH), failure.completedOperations)

            viewModel.onAction(StudioEditAction.Retry)

            assertIs<StudioEditUiState.Success>(viewModel.uiState.value)
            assertEquals(2, repository.updateCalls)
        }

    companion object {
        val STUDIO_ID = InstructorStudioId("42")
        val REMOTE_IMAGE = StudioImageSelection.Remote("https://cdn.classitda.com/studio.jpg")
        val STUDIO =
            ManagedStudio(
                id = STUDIO_ID,
                name = "클래스잇다 스튜디오",
                address = StudioAddress(zoneCode = "13494", roadAddress = "경기 성남시 분당구 판교역로 166"),
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
) : InstructorStudioRepository {
    var lastStudioId: InstructorStudioId? = null
        private set
    var lastDraft: StudioRegistrationDraft? = null
        private set
    var lastImageMutation: StudioImageMutation? = null
        private set
    var updateCalls: Int = 0
        private set

    override suspend fun getStudios(): InstructorMyPageResult<StudioList> = error("not used")

    override suspend fun getStudio(studioId: InstructorStudioId): InstructorMyPageResult<ManagedStudio> =
        InstructorMyPageResult.Success(StudioEditViewModelTest.STUDIO)

    override suspend fun registerStudio(draft: StudioRegistrationDraft): InstructorMyPageResult<Unit> =
        error("not used")

    override suspend fun updateStudio(
        studioId: InstructorStudioId,
        original: ManagedStudio,
        draft: StudioRegistrationDraft,
        imageMutation: StudioImageMutation,
    ): InstructorMyPageResult<Unit> {
        updateCalls += 1
        lastStudioId = studioId
        lastDraft = draft
        lastImageMutation = imageMutation
        return updateResults.removeFirst()
    }
}

private suspend inline fun <reified T> kotlinx.coroutines.flow.StateFlow<*>.awaitState(): T =
    withTimeout(5_000) {
        first { it is T } as T
    }
