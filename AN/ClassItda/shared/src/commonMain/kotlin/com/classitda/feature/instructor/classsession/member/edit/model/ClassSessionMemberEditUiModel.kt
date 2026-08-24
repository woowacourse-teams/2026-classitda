package com.classitda.feature.instructor.classsession.member.edit.model

import com.classitda.feature.instructor.classsession.detail.model.ClassSessionDetailUiModel
import com.classitda.feature.instructor.classsession.detail.model.ClassSessionMemberUiModel

internal data class ClassSessionMemberEditUiModel(
    val detail: ClassSessionDetailUiModel,
    val availableMembers: List<ClassSessionMemberUiModel>,
)
