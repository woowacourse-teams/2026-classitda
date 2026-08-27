package com.classitda.feature.instructor.classsession.edit

import com.classitda.feature.instructor.classsession.edit.model.ClassSessionEditFormUiModel
import com.classitda.domain.model.instructor.management.ClassType as DomainClassType

internal sealed interface ClassSessionEditUiState {
    data object Loading : ClassSessionEditUiState

    data class Success(
        val form: ClassSessionEditFormUiModel,
        val categories: List<DomainClassType>,
    ) : ClassSessionEditUiState

    data class Error(
        val message: String?,
    ) : ClassSessionEditUiState
}
