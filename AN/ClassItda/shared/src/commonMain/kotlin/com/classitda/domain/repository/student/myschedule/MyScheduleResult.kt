package com.classitda.domain.repository.student.myschedule

sealed interface MyScheduleResult<out T> {
    data class Success<T>(
        val value: T,
    ) : MyScheduleResult<T>

    data class Failure(
        val reason: MyScheduleFailureReason,
    ) : MyScheduleResult<Nothing>
}

enum class MyScheduleFailureReason {
    NETWORK,
    NOT_FOUND,
    CONFLICT,
    CANCELLATION_NOT_ALLOWED,
    UNKNOWN,
}
