package com.classitda.feature.instructor.schedule

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.classitda.domain.model.instructor.management.ClassSession
import com.classitda.domain.repository.instructor.management.ClassManagementRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

internal class InstructorScheduleViewModel(
    private val repository: ClassManagementRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow<InstructorScheduleUiState>(InstructorScheduleUiState.Loading)
    val uiState: StateFlow<InstructorScheduleUiState> = _uiState.asStateFlow()

    init {
        load()
    }

    fun retry() {
        load()
    }

    private fun load() {
        _uiState.value = InstructorScheduleUiState.Loading
        viewModelScope.launch {
            runCatching { repository.getSessions() }
                .onSuccess { sessions -> _uiState.value = InstructorScheduleUiState.Success(sessions) }
                .onFailure { error -> _uiState.value = InstructorScheduleUiState.Error(error.message) }
        }
    }
}

internal sealed interface InstructorScheduleUiState {
    data object Loading : InstructorScheduleUiState

    data class Success(
        val sessions: List<ClassSession>,
    ) : InstructorScheduleUiState

    data class Error(
        val message: String?,
    ) : InstructorScheduleUiState
}
