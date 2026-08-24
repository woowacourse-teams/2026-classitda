package com.classitda.feature.instructor.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.classitda.domain.model.instructor.management.ClassSession
import com.classitda.domain.repository.instructor.management.ClassManagementRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

internal class InstructorHomeViewModel(
    private val repository: ClassManagementRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow<InstructorHomeUiState>(InstructorHomeUiState.Loading)
    val uiState: StateFlow<InstructorHomeUiState> = _uiState.asStateFlow()

    init {
        load()
    }

    fun retry() {
        load()
    }

    private fun load() {
        _uiState.value = InstructorHomeUiState.Loading
        viewModelScope.launch {
            runCatching { repository.getSessions() }
                .onSuccess { sessions ->
                    _uiState.value = InstructorHomeUiState.Success(sessions.sortedBy { it.startAt })
                }.onFailure { error ->
                    _uiState.value = InstructorHomeUiState.Error(error.message)
                }
        }
    }
}

internal sealed interface InstructorHomeUiState {
    data object Loading : InstructorHomeUiState

    data class Success(
        val sessions: List<ClassSession>,
    ) : InstructorHomeUiState

    data class Error(
        val message: String?,
    ) : InstructorHomeUiState
}
