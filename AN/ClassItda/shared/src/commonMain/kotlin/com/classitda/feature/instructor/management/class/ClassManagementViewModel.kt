package com.classitda.feature.instructor.management.`class`

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.classitda.domain.model.instructor.management.ClassSession
import com.classitda.domain.model.instructor.management.ClassTemplate
import com.classitda.domain.repository.instructor.management.ClassManagementRepository
import com.classitda.feature.instructor.management.`class`.component.ClassCategoryFilter
import com.classitda.feature.instructor.management.`class`.component.ClassManagementTopTab
import com.classitda.feature.instructor.management.`class`.util.toSessionGroupUiModels
import com.classitda.feature.instructor.management.`class`.util.toUiModel
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

internal class ClassManagementViewModel(
    private val repository: ClassManagementRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow<ClassManagementUiState>(ClassManagementUiState.Loading)
    val uiState: StateFlow<ClassManagementUiState> = _uiState.asStateFlow()

    init {
        loadClassManagement()
    }

    fun onRetry() {
        loadClassManagement()
    }

    fun deleteTemplate(id: String) {
        viewModelScope.launch {
            runCatching { repository.deleteTemplate(id) }
                .onSuccess { loadClassManagement() }
        }
    }

    fun onTopTabSelected(tab: ClassManagementTopTab) {
        _uiState.update { state ->
            if (state is ClassManagementUiState.Success) {
                state.copy(content = state.content.copy(selectedTopTab = tab))
            } else {
                state
            }
        }
    }

    fun onFilterSelected(filterLabel: String) {
        _uiState.update { state ->
            if (state is ClassManagementUiState.Success) {
                state.copy(content = state.content.copy(selectedFilterLabel = filterLabel))
            } else {
                state
            }
        }
    }

    private fun loadClassManagement() {
        val previousSelection =
            (_uiState.value as? ClassManagementUiState.Success)
                ?.content
                ?.let { it.selectedTopTab to it.selectedFilterLabel }

        _uiState.update { ClassManagementUiState.Loading }
        viewModelScope.launch {
            runCatching { fetchClassManagementSnapshot() }
                .onSuccess { snapshot ->
                    _uiState.update {
                        ClassManagementUiState.Success(
                            ClassManagementContentUiModel(
                                templates = snapshot.templates.map { it.toUiModel() },
                                sessionGroups = snapshot.sessions.toSessionGroupUiModels(),
                                customCategories = snapshot.customCategories,
                                selectedTopTab = previousSelection?.first ?: ClassManagementTopTab.TEMPLATE,
                                selectedFilterLabel = previousSelection?.second ?: ClassCategoryFilter.ALL.label,
                            ),
                        )
                    }
                }.onFailure { error ->
                    _uiState.update { ClassManagementUiState.Error(error.message) }
                }
        }
    }

    private suspend fun fetchClassManagementSnapshot(): ClassManagementSnapshot =
        coroutineScope {
            val templatesDeferred = async { repository.getTemplates() }
            val sessionsDeferred = async { repository.getSessions() }
            val customCategoriesDeferred = async { repository.getCustomCategories() }
            ClassManagementSnapshot(
                templates = templatesDeferred.await(),
                sessions = sessionsDeferred.await(),
                customCategories = customCategoriesDeferred.await(),
            )
        }

    private data class ClassManagementSnapshot(
        val templates: List<ClassTemplate>,
        val sessions: List<ClassSession>,
        val customCategories: List<String>,
    )
}
