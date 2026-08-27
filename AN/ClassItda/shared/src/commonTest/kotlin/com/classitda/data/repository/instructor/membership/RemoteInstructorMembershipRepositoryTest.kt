package com.classitda.data.repository.instructor.membership

import com.classitda.core.auth.InMemoryAuthTokenStorage
import com.classitda.core.network.createClassItdaHttpClient
import com.classitda.data.remote.instructor.member.InstructorMemberApi
import com.classitda.domain.model.auth.signup.LoginTokens
import com.classitda.domain.model.studio.StudioId
import com.classitda.domain.repository.instructor.mypage.InstructorMyPageFailureReason
import com.classitda.domain.repository.instructor.mypage.InstructorMyPageResult
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.http.HttpHeaders
import io.ktor.http.headersOf
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class RemoteInstructorMembershipRepositoryTest {
    @Test
    fun `hasNext가 true인데 nextCursor가 없으면 계약 오류로 반환한다`() =
        runBlocking {
            val engine =
                MockEngine {
                    respond(
                        """{"items":[],"hasNext":true,"nextCursor":null}""",
                        headers = headersOf(HttpHeaders.ContentType, "application/json"),
                    )
                }
            val client =
                createClassItdaHttpClient(
                    engine = engine,
                    baseUrl = "https://api.classitda.test/",
                    tokenStorage =
                        InMemoryAuthTokenStorage().apply {
                            write(
                                LoginTokens(
                                    accessToken = "access-token",
                                    accessTokenExpiresInSeconds = 3600,
                                    refreshToken = "refresh-token",
                                    refreshTokenExpiresInSeconds = 86400,
                                ),
                            )
                        },
                )

            try {
                val repository = RemoteInstructorMembershipRepository(InstructorMemberApi(client))
                val result = repository.getStudents(StudioId("42"), null, 10)

                val failure = assertIs<InstructorMyPageResult.Failure>(result)
                assertEquals(InstructorMyPageFailureReason.CONTRACT, failure.reason)
            } finally {
                client.close()
            }
        }
}
