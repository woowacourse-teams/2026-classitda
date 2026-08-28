package com.classitda.domain.model.instructor.mypage

data class InstructorAccountProfile(
    val name: String,
    val phoneNumber: String,
    val email: String?,
    val profileImageUrl: String? = null,
)

data class InstructorMyPageSummary(
    val name: String,
    val phoneNumber: String,
)
