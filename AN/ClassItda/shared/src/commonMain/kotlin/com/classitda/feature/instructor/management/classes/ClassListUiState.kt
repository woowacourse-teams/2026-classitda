package com.classitda.feature.instructor.management.classes

import com.classitda.feature.instructor.management.classes.model.ClassSessionGroupUiModel

internal sealed interface ClassListUiState {
    data object InitialLoading : ClassListUiState

    data class Success(
        val content: ClassListContentUiModel,
        val isRefreshing: Boolean = false,
    ) : ClassListUiState

    data class Error(
        val message: String?,
    ) : ClassListUiState
}

internal data class ClassListContentUiModel(
    val sessionGroups: List<ClassSessionGroupUiModel>,
    val customCategories: List<String>,
    val selectedFilterLabel: String,
)
