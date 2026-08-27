package com.classitda.feature.instructor.management.classtemplates.create

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.classitda.core.studio.InstructorStudioContext
import com.classitda.domain.model.instructor.management.ClassType
import com.classitda.domain.repository.instructor.management.ClassTemplateManagementRepository
import com.classitda.feature.instructor.management.classtemplates.create.model.ClassTemplateDraftUiModel
import com.classitda.feature.instructor.management.classtemplates.create.model.ClassTemplateFormValues
import com.classitda.feature.instructor.management.util.toClassTemplate
import com.classitda.feature.instructor.management.util.toFormValues
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

internal sealed interface ClassTemplateFormLoadState {
    data object Loading : ClassTemplateFormLoadState

    data class Ready(
        val classTypes: List<ClassType>,
        val initialValues: ClassTemplateFormValues?,
    ) : ClassTemplateFormLoadState
}

internal class ClassTemplateCreateViewModel(
    private val templateId: String?,
    private val repository: ClassTemplateManagementRepository,
    private val studioContext: InstructorStudioContext,
) : ViewModel() {
    val isEditMode: Boolean = templateId != null

    private val _formLoadState = MutableStateFlow<ClassTemplateFormLoadState>(ClassTemplateFormLoadState.Loading)
    val formLoadState: StateFlow<ClassTemplateFormLoadState> = _formLoadState.asStateFlow()

    private val _uiState = MutableStateFlow<ClassTemplateCreateUiState>(ClassTemplateCreateUiState.Idle)
    val uiState: StateFlow<ClassTemplateCreateUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            val studioId = studioContext.getSelectedStudio().id.value
            val classTypes = repository.getClassTypes(studioId)
            val initialValues = templateId?.let { repository.getTemplate(studioId, it)?.toFormValues(classTypes) }
            _formLoadState.update { ClassTemplateFormLoadState.Ready(classTypes, initialValues) }
        }
    }

    fun submit(draft: ClassTemplateDraftUiModel) {
        _uiState.update { ClassTemplateCreateUiState.Submitting }
        viewModelScope.launch {
            runCatching {
                val studioId = studioContext.getSelectedStudio().id.value
                if (templateId != null) {
                    repository.updateTemplate(studioId, draft.toClassTemplate(id = templateId))
                } else {
                    repository.createTemplate(studioId, draft.toClassTemplate(id = ""))
                }
            }.onSuccess { _uiState.update { ClassTemplateCreateUiState.Success } }
                .onFailure { error -> _uiState.update { ClassTemplateCreateUiState.Error(error.message) } }
        }
    }
}
