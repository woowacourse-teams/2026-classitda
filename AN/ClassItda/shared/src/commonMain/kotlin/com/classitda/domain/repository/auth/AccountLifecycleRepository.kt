package com.classitda.domain.repository.auth

interface InstructorAccountLifecycleRepository {
    suspend fun withdraw(): AccountLifecycleResult
}

sealed interface AccountLifecycleResult {
    data object Success : AccountLifecycleResult

    data class Failure(
        val reason: AccountLifecycleFailureReason,
    ) : AccountLifecycleResult
}

enum class AccountLifecycleFailureReason {
    NETWORK,
    UNAUTHORIZED,
    FORBIDDEN,
    NOT_FOUND,
    CONFLICT,
    INVALID_REQUEST,
    SERVER,
    UNKNOWN,
}
