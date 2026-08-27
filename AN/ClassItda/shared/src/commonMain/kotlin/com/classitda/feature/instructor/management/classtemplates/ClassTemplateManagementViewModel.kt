package com.classitda.feature.instructor.management.classtemplates

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.classitda.core.studio.InstructorStudioContext
import com.classitda.domain.model.instructor.management.ClassTemplate
import com.classitda.domain.model.instructor.management.ClassType
import com.classitda.domain.repository.instructor.management.ClassTemplateManagementRepository
import com.classitda.feature.instructor.management.classtemplates.util.toUiModel
import com.classitda.feature.instructor.management.component.CategoryFilter
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

internal class ClassTemplateManagementViewModel(
    private val repository: ClassTemplateManagementRepository,
    private val studioContext: InstructorStudioContext,
) : ViewModel() {
    private val _uiState =
        MutableStateFlow<ClassTemplateManagementUiState>(ClassTemplateManagementUiState.InitialLoading)
    val uiState: StateFlow<ClassTemplateManagementUiState> = _uiState.asStateFlow()

    private val _refreshErrors = MutableSharedFlow<String>()
    val refreshErrors: SharedFlow<String> = _refreshErrors.asSharedFlow()

    init {
        loadTemplates()
    }

    fun onRetry() {
        loadTemplates()
    }

    fun deleteTemplate(id: String) {
        viewModelScope.launch {
            runCatching {
                val studioId = studioContext.getSelectedStudio().id.value
                repository.deleteTemplate(studioId, id)
            }.onSuccess { loadTemplates() }
        }
    }

    fun onFilterSelected(filter: CategoryFilter) {
        _uiState.update { state ->
            if (state is ClassTemplateManagementUiState.Success) {
                state.copy(content = state.content.copy(selectedFilter = filter))
            } else {
                state
            }
        }
    }

    private fun loadTemplates() {
        val previousSuccess = _uiState.value as? ClassTemplateManagementUiState.Success

        _uiState.update {
            if (previousSuccess != null) {
                previousSuccess.copy(isRefreshing = true)
            } else {
                ClassTemplateManagementUiState.InitialLoading
            }
        }

        viewModelScope.launch {
            runCatching { fetchTemplateManagementSnapshot() }
                .onSuccess { snapshot ->
                    _uiState.update {
                        ClassTemplateManagementUiState.Success(
                            ClassTemplateManagementContentUiModel(
                                templates = snapshot.templates.map { it.toUiModel() },
                                classTypes = snapshot.classTypes,
                                selectedFilter = previousSuccess?.content?.selectedFilter ?: CategoryFilter.All,
                            ),
                        )
                    }
                }.onFailure { error ->
                    if (previousSuccess != null) {
                        _uiState.update { previousSuccess.copy(isRefreshing = false) }
                        _refreshErrors.emit(error.message ?: "새로고침에 실패했어요")
                    } else {
                        _uiState.update { ClassTemplateManagementUiState.Error(error.message) }
                    }
                }
        }
    }

    private suspend fun fetchTemplateManagementSnapshot(): TemplateManagementSnapshot =
        coroutineScope {
            val studioId = studioContext.getSelectedStudio().id.value
            val templatesDeferred = async { repository.getTemplates(studioId) }
            val classTypesDeferred = async { repository.getClassTypes(studioId) }
            TemplateManagementSnapshot(
                templates = templatesDeferred.await(),
                classTypes = classTypesDeferred.await(),
            )
        }

    private data class TemplateManagementSnapshot(
        val templates: List<ClassTemplate>,
        val classTypes: List<ClassType>,
    )
}
