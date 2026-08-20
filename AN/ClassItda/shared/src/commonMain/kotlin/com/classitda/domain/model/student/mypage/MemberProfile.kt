package com.classitda.domain.model.student.mypage

data class MemberProfile(
    val id: MemberId,
    val name: String,
    val phoneNumber: String,
    val email: String,
    val profileImageUrl: String?,
) {
    init {
        require(name.isNotBlank()) { "회원 이름은 비어 있을 수 없습니다." }
    }
}

data class MyPageSummary(
    val profile: MemberProfile,
    val isInstructorSignupBannerVisible: Boolean,
)
