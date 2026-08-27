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
import com.classitda.feature.instructor.mypage.contract.StudioDetailAction
import com.classitda.feature.instructor.mypage.contract.StudioDetailUiError
import com.classitda.feature.instructor.mypage.contract.StudioDetailUiState
import com.classitda.feature.instructor.mypage.contract.StudioManagementAction
import com.classitda.feature.instructor.mypage.contract.StudioManagementUiError
import com.classitda.feature.instructor.mypage.contract.StudioManagementUiState
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.StateFlow
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

class StudioQueryViewModelTest {
    @BeforeTest
    fun setUpMainDispatcher() {
        Dispatchers.setMain(Dispatchers.Unconfined)
    }

    @AfterTest
    fun resetMainDispatcher() {
        Dispatchers.resetMain()
    }

    @Test
    fun `목록은 Loading에서 Content로 전이하고 응답 순서를 유지한다`() =
        runBlocking {
            val pending = CompletableDeferred<InstructorMyPageResult<StudioList>>()
            val repository = QueryStudioRepository(getStudios = { pending.await() })
            val viewModel = StudioManagementViewModel(repository)

            assertIs<StudioManagementUiState.Loading>(viewModel.uiState.value)
            pending.complete(
                InstructorMyPageResult.Success(
                    StudioList(2, listOf(studio(2), studio(1))),
                ),
            )

            val content = viewModel.uiState.awaitState<StudioManagementUiState.Content>()
            assertEquals(listOf("2", "1"), content.page.studios.map { it.id.value })
            assertEquals(
                "13494",
                content.page.studios
                    .first()
                    .address.zoneCode,
            )
            assertIs<StudioImageSelection.Remote>(
                content.page.studios
                    .first()
                    .image
                    ?.selection,
            )
            Unit
        }

    @Test
    fun `빈 목록은 Empty 상태다`() =
        runBlocking {
            val repository =
                QueryStudioRepository(
                    getStudios = { InstructorMyPageResult.Success(StudioList(0, emptyList())) },
                )

            val state =
                StudioManagementViewModel(repository)
                    .uiState
                    .awaitState<StudioManagementUiState.Empty>()

            assertIs<StudioManagementUiState.Empty>(state)
            Unit
        }

    @Test
    fun `목록 오류 뒤 재시도하면 Content로 전이한다`() =
        runBlocking {
            var result: InstructorMyPageResult<StudioList> =
                InstructorMyPageResult.Failure(InstructorMyPageFailureReason.NETWORK)
            val repository = QueryStudioRepository(getStudios = { result })
            val viewModel = StudioManagementViewModel(repository)

            val error = viewModel.uiState.awaitState<StudioManagementUiState.Error>()
            assertEquals(StudioManagementUiError.NETWORK, error.reason)

            result = InstructorMyPageResult.Success(StudioList(1, listOf(studio(1))))
            viewModel.onAction(StudioManagementAction.Retry)

            assertIs<StudioManagementUiState.Content>(
                viewModel.uiState.awaitState<StudioManagementUiState.Content>(),
            )
            Unit
        }

    @Test
    fun `상세는 Loading에서 Content로 전이하고 전체 응답 값을 보존한다`() =
        runBlocking {
            val pending = CompletableDeferred<InstructorMyPageResult<ManagedStudio>>()
            val repository = QueryStudioRepository(getStudio = { pending.await() })
            val viewModel = StudioDetailViewModel(repository, InstructorStudioId("42"))

            assertIs<StudioDetailUiState.Loading>(viewModel.uiState.value)
            pending.complete(InstructorMyPageResult.Success(studio(42)))

            val content = viewModel.uiState.awaitState<StudioDetailUiState.Content>()
            assertEquals("42", content.studio.id.value)
            assertEquals("경기 성남시 분당구 백현동 532", content.studio.address.jibunAddress)
            assertEquals("09:00:00", content.studio.openingTime)
            assertEquals("시설 설명 42", content.studio.description)
        }

    @Test
    fun `상세 404 뒤 재시도하면 Content로 전이한다`() =
        runBlocking {
            var result: InstructorMyPageResult<ManagedStudio> =
                InstructorMyPageResult.Failure(InstructorMyPageFailureReason.NOT_FOUND)
            val repository = QueryStudioRepository(getStudio = { result })
            val viewModel = StudioDetailViewModel(repository, InstructorStudioId("42"))

            val error = viewModel.uiState.awaitState<StudioDetailUiState.Error>()
            assertEquals(StudioDetailUiError.NOT_FOUND, error.reason)

            result = InstructorMyPageResult.Success(studio(42))
            viewModel.onAction(StudioDetailAction.Retry)

            assertIs<StudioDetailUiState.Content>(
                viewModel.uiState.awaitState<StudioDetailUiState.Content>(),
            )
            Unit
        }

    private fun studio(id: Long) =
        ManagedStudio(
            id = InstructorStudioId(id.toString()),
            name = "시설 $id",
            address =
                StudioAddress(
                    zoneCode = "13494",
                    roadAddress = "경기 성남시 분당구 판교역로 166",
                    jibunAddress = "경기 성남시 분당구 백현동 532",
                    buildingName = "카카오 판교 아지트",
                    detailAddress = "3층",
                ),
            image = StudioImageSelection.Remote("https://cdn.classitda.test/$id.webp"),
            phoneNumber = "031-123-4567",
            description = "시설 설명 $id",
            openingTime = "09:00:00",
            closingTime = "22:00:00",
        )
}

private class QueryStudioRepository(
    private val getStudios: suspend () -> InstructorMyPageResult<StudioList> = { error("not used") },
    private val getStudio: suspend (InstructorStudioId) -> InstructorMyPageResult<ManagedStudio> = {
        error("not used")
    },
) : InstructorStudioRepository {
    override suspend fun getStudios(): InstructorMyPageResult<StudioList> = getStudios.invoke()

    override suspend fun getStudio(studioId: InstructorStudioId): InstructorMyPageResult<ManagedStudio> =
        getStudio.invoke(studioId)

    override suspend fun registerStudio(draft: StudioRegistrationDraft): InstructorMyPageResult<Unit> =
        error("not used")

    override suspend fun updateStudio(
        studioId: InstructorStudioId,
        original: ManagedStudio,
        draft: StudioRegistrationDraft,
        imageMutation: StudioImageMutation,
    ): InstructorMyPageResult<Unit> = error("not used")
}

private suspend inline fun <reified T> StateFlow<*>.awaitState(): T =
    withTimeout(5_000) {
        first { it is T } as T
    }
