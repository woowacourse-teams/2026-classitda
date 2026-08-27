package com.classitda.feature.instructor.mypage.member

import com.classitda.core.studio.InstructorStudioContext
import com.classitda.domain.model.instructor.mypage.InstructorMemberId
import com.classitda.domain.model.instructor.mypage.ManagedMember
import com.classitda.domain.model.instructor.mypage.MemberListPage
import com.classitda.domain.model.instructor.mypage.MemberRegistrationDraft
import com.classitda.domain.model.studio.Studio
import com.classitda.domain.model.studio.StudioId
import com.classitda.domain.repository.instructor.membership.InstructorMembershipRepository
import com.classitda.domain.repository.instructor.mypage.InstructorMyPageResult
import com.classitda.domain.repository.studio.StudioRepository
import com.classitda.feature.instructor.mypage.contract.MemberManagementAction
import com.classitda.feature.instructor.mypage.contract.MemberManagementUiError
import com.classitda.feature.instructor.mypage.contract.MemberManagementUiState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
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

@OptIn(ExperimentalCoroutinesApi::class)
class MemberManagementViewModelTest {
    @BeforeTest
    fun setUpMainDispatcher() {
        Dispatchers.setMain(Dispatchers.Unconfined)
    }

    @AfterTest
    fun resetMainDispatcher() {
        Dispatchers.resetMain()
    }

    @Test
    fun `검색어 변경은 재조회 없이 캐시된 전체 목록을 즉시 필터링한다`() =
        runBlocking {
            val repository =
                RecordingMembershipRepository { _, cursor, size ->
                    assertEquals(10, size)
                    if (cursor == null) {
                        InstructorMyPageResult.Success(
                            MemberListPage(
                                totalCount = 1,
                                members = listOf(member("1", "김민수", "01012345678")),
                                nextPageCursor = "next",
                            ),
                        )
                    } else {
                        InstructorMyPageResult.Success(
                            MemberListPage(
                                totalCount = 1,
                                members = listOf(member("2", "박서준", "01098765432")),
                            ),
                        )
                    }
                }
            val viewModel = MemberManagementViewModel(repository, studioContext())

            val initial = viewModel.uiState.awaitState<MemberManagementUiState.Content>()
            assertEquals(2, initial.page.totalCount)
            assertEquals(2, repository.requests.size)

            viewModel.onAction(MemberManagementAction.QueryChanged("010-9876"))

            val searched = assertIs<MemberManagementUiState.Content>(viewModel.uiState.value)
            assertEquals(listOf("박서준"), searched.page.members.map { it.name })
            assertEquals(2, searched.page.totalCount)
            assertEquals(2, repository.requests.size)

            viewModel.onAction(MemberManagementAction.QueryChanged("없는 회원"))

            val searchEmpty = assertIs<MemberManagementUiState.Content>(viewModel.uiState.value)
            assertEquals(2, searchEmpty.page.totalCount)
            assertEquals(emptyList(), searchEmpty.page.members)
        }

    @Test
    fun `새로고침은 캐시를 새 목록으로 교체한다`() =
        runBlocking {
            var currentMembers = listOf(member("1", "김민수", "01012345678"))
            val repository =
                RecordingMembershipRepository { _, cursor, size ->
                    assertEquals(null, cursor)
                    assertEquals(10, size)
                    InstructorMyPageResult.Success(MemberListPage(currentMembers.size, currentMembers))
                }
            val viewModel = MemberManagementViewModel(repository, studioContext())

            viewModel.uiState.awaitState<MemberManagementUiState.Content>()
            currentMembers = listOf(member("2", "박서준", "01098765432"))
            viewModel.refresh()

            val refreshed = viewModel.uiState.awaitState<MemberManagementUiState.Content>()
            assertEquals(listOf("박서준"), refreshed.page.members.map { it.name })
            assertEquals(2, repository.requests.size)
        }

    @Test
    fun `시설이 없으면 회원 목록 API를 호출하지 않고 시설 없음 상태를 보여준다`() =
        runBlocking {
            val repository =
                RecordingMembershipRepository { _, _, _ ->
                    error("시설이 없을 때는 회원 목록을 조회하면 안 됩니다.")
                }
            val viewModel = MemberManagementViewModel(repository, studioContext(emptyList()))

            viewModel.uiState.awaitState<MemberManagementUiState.NoStudio>()
            assertEquals(0, repository.requests.size)
        }

    @Test
    fun `페이지 커서가 순환하면 계약 오류로 조회를 중단한다`() =
        runBlocking {
            val repository =
                RecordingMembershipRepository { _, cursor, _ ->
                    val nextCursor =
                        when (cursor) {
                            null -> "A"
                            "A" -> "B"
                            "B" -> "A"
                            else -> error("예상하지 못한 커서입니다: $cursor")
                        }
                    InstructorMyPageResult.Success(
                        MemberListPage(
                            totalCount = 0,
                            members = emptyList(),
                            nextPageCursor = nextCursor,
                        ),
                    )
                }
            val viewModel = MemberManagementViewModel(repository, studioContext())

            val error = viewModel.uiState.awaitState<MemberManagementUiState.Error>()
            assertEquals(MemberManagementUiError.UNKNOWN, error.reason)
            assertEquals(listOf(null, "A", "B"), repository.requests.map { it.second })
        }

    private fun studioContext(studios: List<Studio> = listOf(studio("42"))): InstructorStudioContext =
        InstructorStudioContext(
            object : StudioRepository {
                override suspend fun getMyStudios(): List<Studio> = studios
            },
        )

    private fun studio(id: String) =
        Studio(
            id = StudioId(id),
            name = "시설 $id",
            address = "서울",
            phoneNumber = "0212345678",
            openTime = null,
            closeTime = null,
            imageUrl = null,
            description = null,
        )

    private fun member(
        id: String,
        name: String,
        phoneNumber: String,
    ) = ManagedMember(InstructorMemberId(id), name, phoneNumber)
}

private class RecordingMembershipRepository(
    private val getPage: suspend (StudioId, String?, Int) -> InstructorMyPageResult<MemberListPage>,
) : InstructorMembershipRepository {
    val requests = mutableListOf<Triple<StudioId, String?, Int>>()

    override suspend fun getStudents(
        studioId: StudioId,
        cursor: String?,
        size: Int,
    ): InstructorMyPageResult<MemberListPage> {
        requests += Triple(studioId, cursor, size)
        return getPage(studioId, cursor, size)
    }

    override suspend fun registerStudent(
        studioId: StudioId,
        draft: MemberRegistrationDraft,
    ): InstructorMyPageResult<Unit> = error("not used")

    override suspend fun getMembership(
        studioId: StudioId,
        membershipId: InstructorMemberId,
    ): InstructorMyPageResult<ManagedMember> = error("not used")

    override suspend fun updateStudent(
        studioId: StudioId,
        membershipId: InstructorMemberId,
        draft: MemberRegistrationDraft,
    ): InstructorMyPageResult<Unit> = error("not used")

    override suspend fun deleteMembership(
        studioId: StudioId,
        membershipId: InstructorMemberId,
    ): InstructorMyPageResult<Unit> = error("not used")
}

private suspend inline fun <reified T> StateFlow<*>.awaitState(): T =
    withTimeout(5_000) {
        first { it is T } as T
    }
