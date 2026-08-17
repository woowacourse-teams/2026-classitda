package com.classitda.feature.student.home

data class InstructorStudioOptionUiModel(
    val id: String,
    val studioName: String,
    val isSelected: Boolean = false,
    val isLead: Boolean = false,
)

data class MemberStudioOptionUiModel(
    val id: String,
    val studioName: String,
    val isSelected: Boolean = false,
)
