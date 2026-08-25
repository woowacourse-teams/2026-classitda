package com.classitda.feature.instructor.management.classes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.classitda.domain.model.instructor.management.ClassSession
import com.classitda.domain.repository.instructor.management.ClassManagementRepository
import com.classitda.feature.instructor.management.classes.util.toSessionGroupUiModels
import com.classitda.feature.instructor.management.component.ClassCategoryFilter
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

internal class ClassListViewModel(
    private val repository: ClassManagementRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow<ClassListUiState>(ClassListUiState.InitialLoading)
    val uiState: StateFlow<ClassListUiState> = _uiState.asStateFlow()

    private val _refreshErrors = MutableSharedFlow<String>()
    val refreshErrors: SharedFlow<String> = _refreshErrors.asSharedFlow()

    init {
        loadSessions()
    }

    fun onRetry() {
        loadSessions()
    }

    fun onFilterSelected(filterLabel: String) {
        _uiState.update { state ->
            if (state is ClassListUiState.Success) {
                state.copy(content = state.content.copy(selectedFilterLabel = filterLabel))
            } else {
                state
            }
        }
    }

    private fun loadSessions() {
        val previousSuccess = _uiState.value as? ClassListUiState.Success

        _uiState.update {
            if (previousSuccess != null) {
                previousSuccess.copy(isRefreshing = true)
            } else {
                ClassListUiState.InitialLoading
            }
        }

        viewModelScope.launch {
            runCatching { fetchClassListSnapshot() }
                .onSuccess { snapshot ->
                    _uiState.update {
                        ClassListUiState.Success(
                            ClassListContentUiModel(
                                sessionGroups = snapshot.sessions.toSessionGroupUiModels(),
                                customCategories = snapshot.customCategories,
                                selectedFilterLabel =
                                    previousSuccess?.content?.selectedFilterLabel ?: ClassCategoryFilter.ALL.label,
                            ),
                        )
                    }
                }.onFailure { error ->
                    if (previousSuccess != null) {
                        _uiState.update { previousSuccess.copy(isRefreshing = false) }
                        _refreshErrors.emit(error.message ?: "새로고침에 실패했어요")
                    } else {
                        _uiState.update { ClassListUiState.Error(error.message) }
                    }
                }
        }
    }

    private suspend fun fetchClassListSnapshot(): ClassListSnapshot =
        coroutineScope {
            val sessionsDeferred = async { repository.getSessions() }
            val customCategoriesDeferred = async { repository.getCustomCategories() }
            ClassListSnapshot(
                sessions = sessionsDeferred.await(),
                customCategories = customCategoriesDeferred.await(),
            )
        }

    private data class ClassListSnapshot(
        val sessions: List<ClassSession>,
        val customCategories: List<String>,
    )
}
