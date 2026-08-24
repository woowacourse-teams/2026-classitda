package com.classitda.feature.instructor.management.lesson.create

internal sealed interface ClassSessionCreateUiState {
    data object Idle : ClassSessionCreateUiState

    data object Submitting : ClassSessionCreateUiState

    data object Success : ClassSessionCreateUiState

    data class Error(
        val message: String?,
    ) : ClassSessionCreateUiState
}
