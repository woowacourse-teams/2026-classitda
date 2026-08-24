package com.classitda.feature.instructor.classsession.member.edit

import com.classitda.feature.instructor.classsession.member.edit.model.ClassSessionMemberEditUiModel

internal sealed interface ClassSessionMemberEditUiState {
    data object Loading : ClassSessionMemberEditUiState

    data class Success(
        val content: ClassSessionMemberEditUiModel,
    ) : ClassSessionMemberEditUiState

    data class Error(
        val message: String?,
    ) : ClassSessionMemberEditUiState
}
