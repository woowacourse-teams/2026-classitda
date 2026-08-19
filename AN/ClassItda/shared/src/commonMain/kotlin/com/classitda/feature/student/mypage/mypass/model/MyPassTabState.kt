package com.classitda.feature.student.mypage.mypass.model

sealed interface MyPassTabState {
    data object Initial : MyPassTabState

    data class Loading(
        val previousPasses: List<MyPassCardUiModel>? = null,
    ) : MyPassTabState

    data class Content(
        val passes: List<MyPassCardUiModel>,
    ) : MyPassTabState

    data class Error(
        val message: String?,
    ) : MyPassTabState
}
