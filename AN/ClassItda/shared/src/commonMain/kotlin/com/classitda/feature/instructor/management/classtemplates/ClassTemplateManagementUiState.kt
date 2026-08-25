package com.classitda.feature.instructor.management.classtemplates

import com.classitda.domain.model.instructor.management.ClassType
import com.classitda.feature.instructor.management.classtemplates.model.ClassTemplateUiModel
import com.classitda.feature.instructor.management.component.CategoryFilter

internal sealed interface ClassTemplateManagementUiState {
    data object InitialLoading : ClassTemplateManagementUiState

    data class Success(
        val content: ClassTemplateManagementContentUiModel,
        val isRefreshing: Boolean = false,
    ) : ClassTemplateManagementUiState

    data class Error(
        val message: String?,
    ) : ClassTemplateManagementUiState
}

internal data class ClassTemplateManagementContentUiModel(
    val templates: List<ClassTemplateUiModel>,
    val classTypes: List<ClassType>,
    val selectedFilter: CategoryFilter,
)
