package com.classitda.domain.repository.student.mypage

sealed interface MyPageResult<out T> {
    data class Success<T>(
        val value: T,
    ) : MyPageResult<T>

    data class Failure(
        val reason: MyPageFailureReason,
    ) : MyPageResult<Nothing>
}

enum class MyPageFailureReason {
    NETWORK,
    NOT_FOUND,
    CONFLICT,
    INVALID_REQUEST,
    VERIFICATION_EXPIRED,
    VERIFICATION_FAILED,
    UNKNOWN,
}
