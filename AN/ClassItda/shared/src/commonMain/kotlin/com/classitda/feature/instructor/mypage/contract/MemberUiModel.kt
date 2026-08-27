package com.classitda.feature.instructor.mypage.contract

import com.classitda.domain.model.instructor.mypage.InstructorMemberId

/** Immutable member data prepared for rendering. Domain member data must not cross this boundary. */
data class MemberUiModel(
    val id: InstructorMemberId,
    val name: String,
    val phoneNumber: String,
    val avatarFallback: String = "?",
    val avatarImageReference: String? = null,
)

data class MemberListUiModel(
    val totalCount: Int,
    val members: List<MemberUiModel>,
)

/** Form values owned by the UI. A ViewModel converts this to the repository draft. */
data class MemberInputUiModel(
    val name: String = "",
    val phoneNumber: String = "",
) {
    val displayPhoneNumber: String
        get() {
            val digits = phoneNumber.filter(Char::isDigit)
            return when {
                digits.length == 11 -> {
                    "${digits.take(3)}-${digits.substring(3, 7)}-${digits.takeLast(4)}"
                }

                digits.length == 10 && digits.startsWith("02") -> {
                    "${digits.take(2)}-${digits.substring(2, 6)}-${digits.takeLast(4)}"
                }

                digits.length == 10 -> {
                    "${digits.take(3)}-${digits.substring(3, 6)}-${digits.takeLast(4)}"
                }

                else -> {
                    phoneNumber
                }
            }
        }
}

enum class MemberSortOption {
    RECENTLY_REGISTERED,
    NAME_ASC,
}
