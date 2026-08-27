package com.classitda.feature.student.mypage.mypassdetail.model

sealed interface MyPassDetailUiState {
    data object Loading : MyPassDetailUiState

    data class Content(
        val detail: MyPassDetailUiModel,
    ) : MyPassDetailUiState

    data class Error(
        val message: String?,
    ) : MyPassDetailUiState

    data object NotFound : MyPassDetailUiState
}
