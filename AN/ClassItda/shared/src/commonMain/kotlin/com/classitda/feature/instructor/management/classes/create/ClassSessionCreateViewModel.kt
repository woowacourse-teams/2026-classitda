package com.classitda.feature.instructor.management.classes.create

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.classitda.core.studio.InstructorStudioContext
import com.classitda.domain.repository.instructor.management.ClassManagementRepository
import com.classitda.domain.repository.instructor.management.ClassTemplateManagementRepository
import com.classitda.feature.instructor.management.classes.create.model.ClassSessionDraftUiModel
import com.classitda.feature.instructor.management.classtemplates.util.toUiModel
import com.classitda.feature.instructor.management.util.toCreateRequest
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

internal class ClassSessionCreateViewModel(
    private val sessionRepository: ClassManagementRepository,
    private val templateRepository: ClassTemplateManagementRepository,
    private val studioContext: InstructorStudioContext,
) : ViewModel() {
    private val _formLoadState =
        MutableStateFlow<ClassSessionCreateFormLoadState>(ClassSessionCreateFormLoadState.Loading)
    val formLoadState: StateFlow<ClassSessionCreateFormLoadState> = _formLoadState.asStateFlow()

    private val _uiState = MutableStateFlow<ClassSessionCreateUiState>(ClassSessionCreateUiState.Idle)
    val uiState: StateFlow<ClassSessionCreateUiState> = _uiState.asStateFlow()

    init {
        loadForm()
    }

    fun onRetry() {
        loadForm()
    }

    private fun loadForm() {
        _formLoadState.update { ClassSessionCreateFormLoadState.Loading }
        viewModelScope.launch {
            runCatching {
                coroutineScope {
                    val studioId = studioContext.getSelectedStudio().id.value
                    val templatesDeferred = async { templateRepository.getTemplates(studioId) }
                    val classTypesDeferred = async { templateRepository.getClassTypes(studioId) }
                    ClassSessionCreateFormLoadState.Ready(
                        templates = templatesDeferred.await().map { it.toUiModel() },
                        classTypes = classTypesDeferred.await(),
                    )
                }
            }.onSuccess { state -> _formLoadState.update { state } }
                .onFailure { error -> _formLoadState.update { ClassSessionCreateFormLoadState.Error(error.message) } }
        }
    }

    fun submit(draft: ClassSessionDraftUiModel) {
        val classTypeId = draft.category?.id
        if (classTypeId == null) {
            _uiState.update { ClassSessionCreateUiState.Error("카테고리에서 실제 수업 종류를 하나 이상 선택해주세요") }
            return
        }

        _uiState.update { ClassSessionCreateUiState.Submitting }
        viewModelScope.launch {
            runCatching { sessionRepository.createSession(draft.toCreateRequest(classTypeId)) }
                .onSuccess { _uiState.update { ClassSessionCreateUiState.Success } }
                .onFailure { error -> _uiState.update { ClassSessionCreateUiState.Error(error.message) } }
        }
    }
}
