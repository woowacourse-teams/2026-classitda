package com.classitda.feature.instructor.management.lesson

import com.classitda.feature.instructor.management.lesson.component.ClassManagementTopTab
import com.classitda.feature.instructor.management.lesson.model.ClassSessionGroupUiModel
import com.classitda.feature.instructor.management.lesson.model.ClassTemplateUiModel

internal sealed interface ClassManagementUiState {
    data object Loading : ClassManagementUiState

    data class Success(
        val content: ClassManagementContentUiModel,
    ) : ClassManagementUiState

    data class Error(
        val message: String?,
    ) : ClassManagementUiState
}

internal data class ClassManagementContentUiModel(
    val templates: List<ClassTemplateUiModel>,
    val sessionGroups: List<ClassSessionGroupUiModel>,
    val customCategories: List<String>,
    val selectedTopTab: ClassManagementTopTab,
    val selectedFilterLabel: String,
)
