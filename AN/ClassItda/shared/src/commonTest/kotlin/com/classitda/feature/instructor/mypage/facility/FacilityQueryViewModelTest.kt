package com.classitda.feature.instructor.mypage.facility

import com.classitda.domain.model.instructor.mypage.FacilityAddress
import com.classitda.domain.model.instructor.mypage.FacilityImageSelection
import com.classitda.domain.model.instructor.mypage.FacilityRegistrationDraft
import com.classitda.domain.model.instructor.mypage.InstructorFacilityId
import com.classitda.domain.model.instructor.mypage.ManagedFacility
import com.classitda.domain.repository.instructor.mypage.FacilityList
import com.classitda.domain.repository.instructor.mypage.InstructorFacilityRepository
import com.classitda.domain.repository.instructor.mypage.InstructorMyPageFailureReason
import com.classitda.domain.repository.instructor.mypage.InstructorMyPageResult
import com.classitda.feature.instructor.mypage.contract.FacilityDetailAction
import com.classitda.feature.instructor.mypage.contract.FacilityDetailUiError
import com.classitda.feature.instructor.mypage.contract.FacilityDetailUiState
import com.classitda.feature.instructor.mypage.contract.FacilityManagementAction
import com.classitda.feature.instructor.mypage.contract.FacilityManagementUiError
import com.classitda.feature.instructor.mypage.contract.FacilityManagementUiState
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

class FacilityQueryViewModelTest {
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
            val pending = CompletableDeferred<InstructorMyPageResult<FacilityList>>()
            val repository = QueryFacilityRepository(getFacilities = { pending.await() })
            val viewModel = FacilityManagementViewModel(repository)

            assertIs<FacilityManagementUiState.Loading>(viewModel.uiState.value)
            pending.complete(
                InstructorMyPageResult.Success(
                    FacilityList(2, listOf(facility(2), facility(1))),
                ),
            )

            val content = viewModel.uiState.awaitState<FacilityManagementUiState.Content>()
            assertEquals(listOf("2", "1"), content.page.facilities.map { it.id.value })
            assertEquals(
                "13494",
                content.page.facilities
                    .first()
                    .address.zoneCode,
            )
            assertIs<FacilityImageSelection.Remote>(
                content.page.facilities
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
                QueryFacilityRepository(
                    getFacilities = { InstructorMyPageResult.Success(FacilityList(0, emptyList())) },
                )

            val state =
                FacilityManagementViewModel(repository)
                    .uiState
                    .awaitState<FacilityManagementUiState.Empty>()

            assertIs<FacilityManagementUiState.Empty>(state)
            Unit
        }

    @Test
    fun `목록 오류 뒤 재시도하면 Content로 전이한다`() =
        runBlocking {
            var result: InstructorMyPageResult<FacilityList> =
                InstructorMyPageResult.Failure(InstructorMyPageFailureReason.NETWORK)
            val repository = QueryFacilityRepository(getFacilities = { result })
            val viewModel = FacilityManagementViewModel(repository)

            val error = viewModel.uiState.awaitState<FacilityManagementUiState.Error>()
            assertEquals(FacilityManagementUiError.NETWORK, error.reason)

            result = InstructorMyPageResult.Success(FacilityList(1, listOf(facility(1))))
            viewModel.onAction(FacilityManagementAction.Retry)

            assertIs<FacilityManagementUiState.Content>(
                viewModel.uiState.awaitState<FacilityManagementUiState.Content>(),
            )
            Unit
        }

    @Test
    fun `상세는 Loading에서 Content로 전이하고 전체 응답 값을 보존한다`() =
        runBlocking {
            val pending = CompletableDeferred<InstructorMyPageResult<ManagedFacility>>()
            val repository = QueryFacilityRepository(getFacility = { pending.await() })
            val viewModel = FacilityDetailViewModel(repository, InstructorFacilityId("42"))

            assertIs<FacilityDetailUiState.Loading>(viewModel.uiState.value)
            pending.complete(InstructorMyPageResult.Success(facility(42)))

            val content = viewModel.uiState.awaitState<FacilityDetailUiState.Content>()
            assertEquals("42", content.facility.id.value)
            assertEquals("경기 성남시 분당구 백현동 532", content.facility.address.jibunAddress)
            assertEquals("09:00:00", content.facility.openingTime)
            assertEquals("시설 설명 42", content.facility.description)
        }

    @Test
    fun `상세 404 뒤 재시도하면 Content로 전이한다`() =
        runBlocking {
            var result: InstructorMyPageResult<ManagedFacility> =
                InstructorMyPageResult.Failure(InstructorMyPageFailureReason.NOT_FOUND)
            val repository = QueryFacilityRepository(getFacility = { result })
            val viewModel = FacilityDetailViewModel(repository, InstructorFacilityId("42"))

            val error = viewModel.uiState.awaitState<FacilityDetailUiState.Error>()
            assertEquals(FacilityDetailUiError.NOT_FOUND, error.reason)

            result = InstructorMyPageResult.Success(facility(42))
            viewModel.onAction(FacilityDetailAction.Retry)

            assertIs<FacilityDetailUiState.Content>(
                viewModel.uiState.awaitState<FacilityDetailUiState.Content>(),
            )
            Unit
        }

    private fun facility(id: Long) =
        ManagedFacility(
            id = InstructorFacilityId(id.toString()),
            name = "시설 $id",
            address =
                FacilityAddress(
                    zoneCode = "13494",
                    roadAddress = "경기 성남시 분당구 판교역로 166",
                    jibunAddress = "경기 성남시 분당구 백현동 532",
                    buildingName = "카카오 판교 아지트",
                    detailAddress = "3층",
                ),
            image = FacilityImageSelection.Remote("https://cdn.classitda.test/$id.webp"),
            phoneNumber = "031-123-4567",
            description = "시설 설명 $id",
            openingTime = "09:00:00",
            closingTime = "22:00:00",
        )
}

private class QueryFacilityRepository(
    private val getFacilities: suspend () -> InstructorMyPageResult<FacilityList> = { error("not used") },
    private val getFacility: suspend (InstructorFacilityId) -> InstructorMyPageResult<ManagedFacility> = {
        error("not used")
    },
) : InstructorFacilityRepository {
    override suspend fun getFacilities(): InstructorMyPageResult<FacilityList> = getFacilities.invoke()

    override suspend fun getFacility(facilityId: InstructorFacilityId): InstructorMyPageResult<ManagedFacility> =
        getFacility.invoke(facilityId)

    override suspend fun registerFacility(draft: FacilityRegistrationDraft): InstructorMyPageResult<Unit> =
        error("not used")

    override suspend fun updateFacility(
        facilityId: InstructorFacilityId,
        draft: FacilityRegistrationDraft,
    ): InstructorMyPageResult<Unit> = error("not used")
}

private suspend inline fun <reified T> StateFlow<*>.awaitState(): T =
    withTimeout(5_000) {
        first { it is T } as T
    }
