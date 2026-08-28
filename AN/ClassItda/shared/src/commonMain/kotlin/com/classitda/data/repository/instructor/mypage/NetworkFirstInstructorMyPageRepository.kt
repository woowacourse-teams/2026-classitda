package com.classitda.data.repository.instructor.mypage

import co.touchlab.kermit.Logger
import com.classitda.data.local.instructor.mypage.InstructorMyPageCache
import com.classitda.domain.model.instructor.mypage.InstructorMyPageSummary
import com.classitda.domain.repository.instructor.mypage.InstructorMyPageFailureReason
import com.classitda.domain.repository.instructor.mypage.InstructorMyPageResult
import com.classitda.domain.repository.instructor.mypage.InstructorMyPageSummaryRepository
import com.classitda.domain.repository.instructor.mypage.InstructorProfileRepository
import kotlinx.coroutines.CancellationException

internal class NetworkFirstInstructorMyPageRepository(
    private val remoteProfileRepository: InstructorProfileRepository,
    private val cache: InstructorMyPageCache,
) : InstructorMyPageSummaryRepository {
    override suspend fun getSummary(): InstructorMyPageResult<InstructorMyPageSummary> =
        when (val remote = remoteProfileRepository.getProfile()) {
            is InstructorMyPageResult.Success -> {
                val summary =
                    InstructorMyPageSummary(
                        name = remote.value.name,
                        phoneNumber = remote.value.phoneNumber,
                    )
                writeCacheBestEffort(summary)
                InstructorMyPageResult.Success(summary)
            }

            is InstructorMyPageResult.Failure -> {
                if (remote.reason != InstructorMyPageFailureReason.NETWORK) {
                    remote
                } else {
                    readCacheOr(remote.reason)
                }
            }
        }

    private suspend fun writeCacheBestEffort(summary: InstructorMyPageSummary) {
        try {
            cache.replace(summary)
        } catch (exception: CancellationException) {
            throw exception
        } catch (exception: Throwable) {
            Logger.w(exception) { "InstructorMyPageCache: write failed" }
        }
    }

    private suspend fun readCacheOr(
        originalFailureReason: InstructorMyPageFailureReason,
    ): InstructorMyPageResult<InstructorMyPageSummary> =
        try {
            cache.read()?.let { summary -> InstructorMyPageResult.Success(summary) }
                ?: InstructorMyPageResult.Failure(originalFailureReason)
        } catch (exception: CancellationException) {
            throw exception
        } catch (exception: Throwable) {
            Logger.w(exception) { "InstructorMyPageCache: read failed" }
            InstructorMyPageResult.Failure(originalFailureReason)
        }
}
