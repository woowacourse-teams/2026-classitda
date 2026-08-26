package com.classitda.feature.instructor.mypage.studio

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.classitda.domain.repository.instructor.mypage.InstructorMyPageResult
import com.classitda.domain.repository.instructor.mypage.InstructorStudioRepository
import com.classitda.feature.instructor.mypage.contract.StudioManagementAction
import com.classitda.feature.instructor.mypage.contract.StudioManagementUiError
import com.classitda.feature.instructor.mypage.contract.StudioManagementUiState
import com.classitda.feature.instructor.mypage.toStudioError
import com.classitda.feature.instructor.mypage.toStudioListUiModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

internal class StudioManagementViewModel(
    private val repository: InstructorStudioRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow<StudioManagementUiState>(StudioManagementUiState.Loading)
    val uiState: StateFlow<StudioManagementUiState> = _uiState.asStateFlow()

    init {
        load()
    }

    fun onAction(action: StudioManagementAction) {
        if (action == StudioManagementAction.Retry) load()
    }

    fun refresh() {
        load()
    }

    private fun load() {
        _uiState.value = StudioManagementUiState.Loading
        viewModelScope.launch {
            _uiState.value =
                when (val result = repository.getStudios()) {
                    is InstructorMyPageResult.Success -> {
                        if (result.value.studios.isEmpty()) {
                            StudioManagementUiState.Empty
                        } else {
                            StudioManagementUiState
                                .Content(
                                    result.value.toStudioListUiModel(),
                                )
                        }
                    }

                    is InstructorMyPageResult.Failure -> {
                        StudioManagementUiState.Error(result.reason.toStudioError())
                    }
                }
        }
    }
}
