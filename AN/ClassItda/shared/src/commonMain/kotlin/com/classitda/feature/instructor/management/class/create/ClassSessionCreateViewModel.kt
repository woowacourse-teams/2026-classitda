package com.classitda.feature.instructor.management.`class`.create

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.classitda.domain.repository.instructor.management.ClassManagementRepository
import com.classitda.feature.instructor.management.`class`.create.model.ClassSessionDraftUiModel
import com.classitda.feature.instructor.management.`class`.create.util.toClassSessions
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

internal class ClassSessionCreateViewModel(
    private val repository: ClassManagementRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow<ClassSessionCreateUiState>(ClassSessionCreateUiState.Idle)
    val uiState: StateFlow<ClassSessionCreateUiState> = _uiState.asStateFlow()

    fun submit(draft: ClassSessionDraftUiModel) {
        _uiState.update { ClassSessionCreateUiState.Submitting }
        viewModelScope.launch {
            runCatching {
                val sessions = draft.toClassSessions()
                sessions.forEach { repository.createSession(it) }
            }.onSuccess { _uiState.update { ClassSessionCreateUiState.Success } }
                .onFailure { error -> _uiState.update { ClassSessionCreateUiState.Error(error.message) } }
        }
    }
}
