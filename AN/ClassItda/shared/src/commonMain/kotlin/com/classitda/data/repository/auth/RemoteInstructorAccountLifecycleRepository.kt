package com.classitda.data.repository.auth

import co.touchlab.kermit.Logger
import com.classitda.core.auth.SessionCacheCleaner
import com.classitda.data.remote.member.MemberApi
import com.classitda.domain.repository.auth.AccountLifecycleFailureReason
import com.classitda.domain.repository.auth.AccountLifecycleResult
import com.classitda.domain.repository.auth.InstructorAccountLifecycleRepository
import io.ktor.client.plugins.ResponseException
import io.ktor.serialization.JsonConvertException
import kotlinx.coroutines.CancellationException
import kotlinx.io.IOException
import kotlinx.serialization.SerializationException

internal class RemoteInstructorAccountLifecycleRepository(
    private val api: MemberApi,
    private val sessionCacheCleaner: SessionCacheCleaner = SessionCacheCleaner { },
) : InstructorAccountLifecycleRepository {
    override suspend fun withdraw(): AccountLifecycleResult =
        try {
            api.deleteMe()
            runCatching { sessionCacheCleaner.clear() }
                .onFailure { error -> Logger.e("AccountLifecycle: local cache clear failed: ${error.message}") }
            AccountLifecycleResult.Success
        } catch (exception: CancellationException) {
            throw exception
        } catch (exception: ResponseException) {
            AccountLifecycleResult.Failure(exception.toFailureReason())
        } catch (exception: Throwable) {
            AccountLifecycleResult.Failure(exception.toFailureReason())
        }
}

private fun ResponseException.toFailureReason(): AccountLifecycleFailureReason =
    when (response.status.value) {
        400 -> AccountLifecycleFailureReason.INVALID_REQUEST
        401 -> AccountLifecycleFailureReason.UNAUTHORIZED
        403 -> AccountLifecycleFailureReason.FORBIDDEN
        404 -> AccountLifecycleFailureReason.NOT_FOUND
        409 -> AccountLifecycleFailureReason.CONFLICT
        in 500..599 -> AccountLifecycleFailureReason.SERVER
        else -> AccountLifecycleFailureReason.UNKNOWN
    }

private fun Throwable.toFailureReason(): AccountLifecycleFailureReason =
    when (this) {
        is JsonConvertException, is SerializationException -> AccountLifecycleFailureReason.UNKNOWN
        is IOException -> AccountLifecycleFailureReason.NETWORK
        else -> AccountLifecycleFailureReason.UNKNOWN
    }
