package com.classitda.feature.instructor.classsession.edit

import com.classitda.feature.instructor.classsession.edit.model.ClassSessionEditFormUiModel

internal sealed interface ClassSessionEditUiState {
    data object Loading : ClassSessionEditUiState

    data class Success(
        val form: ClassSessionEditFormUiModel,
    ) : ClassSessionEditUiState

    data class Error(
        val message: String?,
    ) : ClassSessionEditUiState
}
