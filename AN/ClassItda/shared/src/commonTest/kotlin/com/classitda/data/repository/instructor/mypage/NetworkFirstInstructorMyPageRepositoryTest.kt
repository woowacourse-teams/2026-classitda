package com.classitda.data.repository.instructor.mypage

import com.classitda.data.local.instructor.mypage.InstructorMyPageCache
import com.classitda.domain.model.instructor.mypage.InstructorAccountProfile
import com.classitda.domain.model.instructor.mypage.InstructorMyPageSummary
import com.classitda.domain.repository.instructor.mypage.InstructorMyPageFailureReason
import com.classitda.domain.repository.instructor.mypage.InstructorMyPageResult
import com.classitda.domain.repository.instructor.mypage.InstructorProfileRepository
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class NetworkFirstInstructorMyPageRepositoryTest {
    @Test
    fun `remote 성공 값은 이름과 전화번호만 저장하고 반환한다`() =
        runBlocking {
            val cache = FakeInstructorMyPageCache()
            val repository = NetworkFirstInstructorMyPageRepository(SuccessRemote(), cache)

            val result = repository.getSummary()

            assertEquals(
                InstructorMyPageSummary("최신 이름", "01099998888"),
                assertIs<InstructorMyPageResult.Success<*>>(result).value,
            )
            assertEquals(InstructorMyPageSummary("최신 이름", "01099998888"), cache.value)
        }

    @Test
    fun `network 실패와 cache가 함께 있으면 cache 성공을 반환한다`() =
        runBlocking {
            val cache = FakeInstructorMyPageCache(InstructorMyPageSummary("저장된 이름", "01011112222"))
            val repository =
                NetworkFirstInstructorMyPageRepository(
                    FailingRemote(InstructorMyPageFailureReason.NETWORK),
                    cache,
                )

            val result = repository.getSummary()

            assertEquals(cache.value, assertIs<InstructorMyPageResult.Success<*>>(result).value)
            assertEquals(1, cache.readCalls)
        }

    @Test
    fun `network 실패와 cache가 없으면 원래 network 오류를 반환한다`() =
        runBlocking {
            val cache = FakeInstructorMyPageCache()
            val repository =
                NetworkFirstInstructorMyPageRepository(
                    FailingRemote(InstructorMyPageFailureReason.NETWORK),
                    cache,
                )

            val result = repository.getSummary()

            assertEquals(InstructorMyPageFailureReason.NETWORK, assertIs<InstructorMyPageResult.Failure>(result).reason)
        }

    @Test
    fun `remote 성공 후 cache 저장 실패는 성공 결과를 뒤집지 않는다`() =
        runBlocking {
            val cache = FakeInstructorMyPageCache(writeError = IllegalStateException("write failed"))
            val repository = NetworkFirstInstructorMyPageRepository(SuccessRemote(), cache)

            val result = repository.getSummary()

            assertEquals(
                InstructorMyPageSummary("최신 이름", "01099998888"),
                assertIs<InstructorMyPageResult.Success<*>>(result).value,
            )
        }

    @Test
    fun `network 실패 후 cache 조회 실패는 원래 network 오류를 반환한다`() =
        runBlocking {
            val cache = FakeInstructorMyPageCache(readError = IllegalStateException("read failed"))
            val repository =
                NetworkFirstInstructorMyPageRepository(
                    FailingRemote(InstructorMyPageFailureReason.NETWORK),
                    cache,
                )

            val result = repository.getSummary()

            assertEquals(InstructorMyPageFailureReason.NETWORK, assertIs<InstructorMyPageResult.Failure>(result).reason)
        }

    @Test
    fun `인증 실패는 cache가 있어도 fallback하지 않는다`() =
        runBlocking {
            val cache = FakeInstructorMyPageCache(InstructorMyPageSummary("오래된 이름", "01011112222"))
            val repository =
                NetworkFirstInstructorMyPageRepository(
                    FailingRemote(InstructorMyPageFailureReason.UNAUTHORIZED),
                    cache,
                )

            val result = repository.getSummary()

            assertEquals(
                InstructorMyPageFailureReason.UNAUTHORIZED,
                assertIs<InstructorMyPageResult.Failure>(result).reason,
            )
            assertEquals(0, cache.readCalls)
        }
}

private class SuccessRemote : InstructorProfileRepository {
    override suspend fun getProfile(): InstructorMyPageResult<InstructorAccountProfile> =
        InstructorMyPageResult.Success(
            InstructorAccountProfile(
                name = "최신 이름",
                phoneNumber = "01099998888",
                email = "not-cached@classitda.com",
            ),
        )

    override suspend fun updateProfileName(name: String): InstructorMyPageResult<InstructorAccountProfile> =
        error("테스트에서 호출하지 않습니다")
}

private class FailingRemote(
    private val reason: InstructorMyPageFailureReason,
) : InstructorProfileRepository {
    override suspend fun getProfile(): InstructorMyPageResult<InstructorAccountProfile> =
        InstructorMyPageResult.Failure(reason)

    override suspend fun updateProfileName(name: String): InstructorMyPageResult<InstructorAccountProfile> =
        error("테스트에서 호출하지 않습니다")
}

private class FakeInstructorMyPageCache(
    var value: InstructorMyPageSummary? = null,
    private val readError: Throwable? = null,
    private val writeError: Throwable? = null,
) : InstructorMyPageCache {
    var readCalls: Int = 0
        private set

    override suspend fun read(): InstructorMyPageSummary? {
        readCalls += 1
        readError?.let { throw it }
        return value
    }

    override suspend fun replace(summary: InstructorMyPageSummary) {
        writeError?.let { throw it }
        value = summary
    }

    override suspend fun clear() {
        value = null
    }
}
