package com.classitda.feature.instructor.mypage.profile

import com.classitda.domain.model.instructor.mypage.InstructorAccountProfile
import com.classitda.domain.repository.instructor.mypage.InstructorMyPageResult
import com.classitda.domain.repository.instructor.mypage.InstructorProfileRepository
import com.classitda.feature.common.profile.contract.ProfileEditAction
import com.classitda.feature.common.profile.contract.ProfileEditUiState
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
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class InstructorProfileEditViewModelTest {
    @BeforeTest
    fun setUpMainDispatcher() {
        Dispatchers.setMain(Dispatchers.Unconfined)
    }

    @AfterTest
    fun resetMainDispatcher() {
        Dispatchers.resetMain()
    }

    @Test
    fun `이름은 50자까지 저장할 수 있다`() =
        runBlocking {
            val repository = RecordingProfileRepository()
            val viewModel = InstructorProfileEditViewModel(repository)
            viewModel.uiState.awaitState<ProfileEditUiState.Editing>()

            viewModel.onAction(ProfileEditAction.NameChanged("가".repeat(50)))
            assertTrue(assertIs<ProfileEditUiState.Editing>(viewModel.uiState.value).canSave)

            viewModel.onAction(ProfileEditAction.Save)

            assertEquals(1, repository.updateCalls)
            assertIs<ProfileEditUiState.Editing>(viewModel.uiState.value)
            Unit
        }

    @Test
    fun `이름이 50자를 초과하면 저장할 수 없다`() =
        runBlocking {
            val repository = RecordingProfileRepository()
            val viewModel = InstructorProfileEditViewModel(repository)
            viewModel.uiState.awaitState<ProfileEditUiState.Editing>()

            viewModel.onAction(ProfileEditAction.NameChanged("가".repeat(51)))
            assertFalse(assertIs<ProfileEditUiState.Editing>(viewModel.uiState.value).canSave)

            viewModel.onAction(ProfileEditAction.Save)

            assertEquals(0, repository.updateCalls)
            Unit
        }
}

private class RecordingProfileRepository : InstructorProfileRepository {
    private var profile =
        InstructorAccountProfile(
            name = "기존 이름",
            phoneNumber = "01012345678",
            email = "instructor@classitda.com",
        )
    var updateCalls = 0
        private set

    override suspend fun getProfile(): InstructorMyPageResult<InstructorAccountProfile> =
        InstructorMyPageResult.Success(profile)

    override suspend fun updateProfileName(name: String): InstructorMyPageResult<InstructorAccountProfile> {
        updateCalls += 1
        profile = profile.copy(name = name)
        return InstructorMyPageResult.Success(profile)
    }
}

private suspend inline fun <reified T> StateFlow<*>.awaitState(): T =
    withTimeout(5_000) {
        first { it is T } as T
    }
