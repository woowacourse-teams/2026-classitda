package com.classitda.domain.model.instructor.mypage

import kotlin.time.Instant

data class ManagedMember(
    val id: InstructorMemberId,
    val name: String,
    val phoneNumber: String,
    val profileImageUrl: String? = null,
    val registeredAt: Instant? = null,
)

data class MemberListPage(
    val totalCount: Int,
    val members: List<ManagedMember>,
    val nextPageCursor: String? = null,
) {
    init {
        require(totalCount >= 0) { "회원 총원은 음수일 수 없습니다." }
    }
}

data class MemberRegistrationDraft(
    val name: String = "",
    val phoneNumber: String = "",
)

enum class MemberSortOrder {
    RECENTLY_REGISTERED,
    NAME_ASC,
}
