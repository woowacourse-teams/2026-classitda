package com.classitda.domain.model.instructor.mypage

data class InstructorAccountProfile(
    val id: String,
    val name: String,
    val phoneNumber: String,
    val email: String,
    val profileImageUrl: String?,
) {
    init {
        require(id.isNotBlank()) { "강사 계정 ID는 비어 있을 수 없습니다." }
    }
}

data class InstructorMyPageSummary(
    val profile: InstructorAccountProfile,
)
