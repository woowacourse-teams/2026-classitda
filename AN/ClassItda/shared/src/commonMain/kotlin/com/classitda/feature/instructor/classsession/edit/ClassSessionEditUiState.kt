package com.classitda.feature.instructor.classsession.edit

import com.classitda.domain.model.instructor.management.ClassType
import com.classitda.feature.instructor.classsession.edit.model.ClassSessionEditFormUiModel

internal sealed interface ClassSessionEditUiState {
    data object Loading : ClassSessionEditUiState

    data class Success(
        val form: ClassSessionEditFormUiModel,
        val categories: List<ClassType>,
    ) : ClassSessionEditUiState

    data class Error(
        val message: String?,
    ) : ClassSessionEditUiState
}
