package com.classitda.feature.instructor.mypage.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.classitda.domain.repository.instructor.mypage.InstructorMyPageResult
import com.classitda.domain.repository.instructor.mypage.InstructorProfileRepository
import com.classitda.feature.common.profile.contract.ProfileUiError
import com.classitda.feature.common.profile.contract.ProfileViewAction
import com.classitda.feature.common.profile.contract.ProfileViewUiState
import com.classitda.feature.instructor.mypage.toProfileUiError
import com.classitda.feature.instructor.mypage.toProfileUiModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

internal class InstructorProfileViewModel(
    private val repository: InstructorProfileRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow<ProfileViewUiState>(ProfileViewUiState.Loading)
    val uiState: StateFlow<ProfileViewUiState> = _uiState.asStateFlow()

    init {
        refresh()
    }

    fun onAction(action: ProfileViewAction) {
        if (action == ProfileViewAction.Retry) refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            _uiState.value =
                when (val result = repository.getProfile()) {
                    is InstructorMyPageResult.Success -> ProfileViewUiState.Content(result.value.toProfileUiModel())
                    is InstructorMyPageResult.Failure -> ProfileViewUiState.Error(result.reason.toProfileUiError())
                }
        }
    }
}
