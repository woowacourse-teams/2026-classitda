package com.classitda.feature.instructor.mypage.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.classitda.domain.model.instructor.mypage.InstructorAccountProfile
import com.classitda.domain.repository.instructor.mypage.InstructorMyPageResult
import com.classitda.domain.repository.instructor.mypage.InstructorProfileRepository
import com.classitda.feature.common.profile.contract.ProfileEditAction
import com.classitda.feature.common.profile.contract.ProfileEditUiState
import com.classitda.feature.instructor.mypage.toEditingState
import com.classitda.feature.instructor.mypage.toProfileUiError
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

internal class InstructorProfileEditViewModel(
    private val repository: InstructorProfileRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow<ProfileEditUiState>(ProfileEditUiState.Loading)
    val uiState: StateFlow<ProfileEditUiState> = _uiState.asStateFlow()
    private var profile: InstructorAccountProfile? = null

    init {
        refresh()
    }

    fun onAction(action: ProfileEditAction) {
        when (action) {
            ProfileEditAction.Retry -> refresh()
            is ProfileEditAction.NameChanged -> changeName(action.name)
            ProfileEditAction.Save -> save()
            else -> Unit
        }
    }

    fun refresh() {
        viewModelScope.launch {
            _uiState.value =
                when (val result = repository.getProfile()) {
                    is InstructorMyPageResult.Success -> result.value.toEditingState().also { profile = result.value }
                    is InstructorMyPageResult.Failure -> ProfileEditUiState.Error(result.reason.toProfileUiError())
                }
        }
    }

    private fun changeName(name: String) {
        val current = profile ?: return
        val state = _uiState.value as? ProfileEditUiState.Editing ?: return
        _uiState.value =
            state.copy(
                draftName = name,
                canSave = name.isNotBlank() && name.length <= MAX_NAME_LENGTH && name != current.name,
            )
    }

    private fun save() {
        val current = profile ?: return
        val state = _uiState.value as? ProfileEditUiState.Editing ?: return
        if (!state.canSave) return
        _uiState.value = ProfileEditUiState.Saving(state.profile, state.phoneNumber, state.draftName)
        viewModelScope.launch {
            _uiState.value =
                when (val result = repository.updateProfileName(state.draftName)) {
                    is InstructorMyPageResult.Success -> {
                        result.value.toEditingState().also { profile = result.value }
                    }

                    is InstructorMyPageResult.Failure -> {
                        ProfileEditUiState.SaveFailed(
                            state.profile,
                            current.phoneNumber,
                            state.draftName,
                            result.reason.toProfileUiError(),
                        )
                    }
                }
        }
    }
}

private const val MAX_NAME_LENGTH = 50
