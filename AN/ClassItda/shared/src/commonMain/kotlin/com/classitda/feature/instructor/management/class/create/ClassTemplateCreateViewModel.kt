package com.classitda.feature.instructor.management.`class`.create

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.classitda.domain.repository.instructor.management.ClassManagementRepository
import com.classitda.feature.instructor.management.`class`.create.model.ClassTemplateDraftUiModel
import com.classitda.feature.instructor.management.`class`.create.model.ClassTemplateFormValues
import com.classitda.feature.instructor.management.`class`.create.util.toClassTemplate
import com.classitda.feature.instructor.management.`class`.create.util.toFormValues
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

internal sealed interface ClassTemplateFormLoadState {
    data object Loading : ClassTemplateFormLoadState

    data class Ready(
        val initialValues: ClassTemplateFormValues?,
    ) : ClassTemplateFormLoadState
}

internal class ClassTemplateCreateViewModel(
    private val templateId: String?,
    private val repository: ClassManagementRepository,
) : ViewModel() {
    val isEditMode: Boolean = templateId != null

    private val _formLoadState =
        MutableStateFlow<ClassTemplateFormLoadState>(
            if (templateId == null) ClassTemplateFormLoadState.Ready(null) else ClassTemplateFormLoadState.Loading,
        )
    val formLoadState: StateFlow<ClassTemplateFormLoadState> = _formLoadState.asStateFlow()

    private val _uiState = MutableStateFlow<ClassTemplateCreateUiState>(ClassTemplateCreateUiState.Idle)
    val uiState: StateFlow<ClassTemplateCreateUiState> = _uiState.asStateFlow()

    init {
        if (templateId != null) {
            viewModelScope.launch {
                val template = repository.getTemplate(templateId)
                _formLoadState.update { ClassTemplateFormLoadState.Ready(template?.toFormValues()) }
            }
        }
    }

    fun submit(draft: ClassTemplateDraftUiModel) {
        _uiState.update { ClassTemplateCreateUiState.Submitting }
        viewModelScope.launch {
            runCatching {
                if (templateId != null) {
                    repository.updateTemplate(draft.toClassTemplate(id = templateId))
                } else {
                    repository.createTemplate(draft.toClassTemplate(id = ""))
                }
            }.onSuccess { _uiState.update { ClassTemplateCreateUiState.Success } }
                .onFailure { error -> _uiState.update { ClassTemplateCreateUiState.Error(error.message) } }
        }
    }
}
