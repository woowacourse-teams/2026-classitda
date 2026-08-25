package com.classitda.domain.repository.instructor.mypage

import com.classitda.domain.model.instructor.mypage.InstructorPhoneVerificationId
import com.classitda.domain.model.instructor.mypage.ManagedFacility

sealed interface InstructorMyPageResult<out T> {
    data class Success<T>(
        val value: T,
    ) : InstructorMyPageResult<T>

    data class Failure(
        val reason: InstructorMyPageFailureReason,
    ) : InstructorMyPageResult<Nothing>
}

enum class InstructorMyPageFailureReason {
    NETWORK,
    UNAUTHORIZED,
    FORBIDDEN,
    SERVER,
    NOT_FOUND,
    CONFLICT,
    INVALID_REQUEST,
    UNSUPPORTED_IMAGE,
    IMAGE_TOO_LARGE,
    IMAGE_READ_FAILED,
    UPLOAD_EXPIRED_OR_REJECTED,
    CONTRACT,
    VERIFICATION_EXPIRED,
    VERIFICATION_FAILED,
    UNKNOWN,
}

data class InstructorPhoneVerificationChallenge(
    val verificationId: InstructorPhoneVerificationId,
)

data class FacilityList(
    val totalCount: Int,
    val facilities: List<ManagedFacility>,
) {
    init {
        require(totalCount >= 0) { "시설 총원은 음수일 수 없습니다." }
    }
}
