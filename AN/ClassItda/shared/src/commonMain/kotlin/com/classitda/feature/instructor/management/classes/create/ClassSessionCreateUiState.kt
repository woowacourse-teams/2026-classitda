package com.classitda.feature.instructor.management.classes.create

import com.classitda.domain.model.instructor.management.ClassType
import com.classitda.feature.instructor.management.classtemplates.model.ClassTemplateUiModel

internal sealed interface ClassSessionCreateFormLoadState {
    data object Loading : ClassSessionCreateFormLoadState

    data class Ready(
        val templates: List<ClassTemplateUiModel>,
        val classTypes: List<ClassType>,
    ) : ClassSessionCreateFormLoadState

    data class Error(
        val message: String?,
    ) : ClassSessionCreateFormLoadState
}

internal sealed interface ClassSessionCreateUiState {
    data object Idle : ClassSessionCreateUiState

    data object Submitting : ClassSessionCreateUiState

    data object Success : ClassSessionCreateUiState

    data class Error(
        val message: String?,
    ) : ClassSessionCreateUiState
}
