package com.classitda.domain.model.instructor.management

data class ClassSessionMember(
    val id: String,
    val name: String,
    val isTemporary: Boolean = false,
)
