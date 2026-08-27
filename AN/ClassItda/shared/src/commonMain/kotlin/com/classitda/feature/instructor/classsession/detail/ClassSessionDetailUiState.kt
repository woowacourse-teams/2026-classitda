package com.classitda.feature.instructor.classsession.detail

import com.classitda.feature.instructor.classsession.detail.model.ClassSessionDetailUiModel

internal sealed interface ClassSessionDetailUiState {
    data object Loading : ClassSessionDetailUiState

    data class Success(
        val detail: ClassSessionDetailUiModel,
    ) : ClassSessionDetailUiState

    data class Error(
        val message: String?,
    ) : ClassSessionDetailUiState
}
