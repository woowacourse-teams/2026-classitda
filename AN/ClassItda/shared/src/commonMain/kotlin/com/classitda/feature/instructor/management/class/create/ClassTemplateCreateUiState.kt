package com.classitda.feature.instructor.management.`class`.create

internal sealed interface ClassTemplateCreateUiState {
    data object Idle : ClassTemplateCreateUiState

    data object Submitting : ClassTemplateCreateUiState

    data object Success : ClassTemplateCreateUiState

    data class Error(
        val message: String?,
    ) : ClassTemplateCreateUiState
}
