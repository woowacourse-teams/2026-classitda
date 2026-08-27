package com.classitda.di.instructor.mypage

import com.classitda.core.network.createClassItdaHttpClient
import com.classitda.data.repository.instructor.mypage.DemoInstructorMyPageRepository
import com.classitda.data.repository.instructor.mypage.RemoteInstructorProfileRepository
import com.classitda.data.repository.instructor.mypage.RemoteInstructorStudioRepository
import com.classitda.di.instructor.instructorModule
import com.classitda.domain.repository.instructor.mypage.InstructorMyPageRepository
import com.classitda.domain.repository.instructor.mypage.InstructorProfileRepository
import com.classitda.domain.repository.instructor.mypage.InstructorStudioRepository
import com.classitda.domain.repository.instructor.mypage.StudioImageUploader
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import org.koin.core.qualifier.named
import org.koin.dsl.koinApplication
import org.koin.dsl.module
import kotlin.test.Test
import kotlin.test.assertIs

class InstructorStudioRemoteModuleTest {
    @Test
    fun `시설과 프로필은 Remote binding이고 회원 관리는 Demo binding이다`() {
        val application =
            koinApplication {
                modules(
                    module {
                        single<HttpClient> {
                            createClassItdaHttpClient(
                                MockEngine { error("DI smoke test는 네트워크를 호출하지 않습니다") },
                                BASE_URL,
                            )
                        }
                    },
                    instructorModule,
                    instructorMyPageDemoModule,
                )
            }

        try {
            assertIs<RemoteInstructorStudioRepository>(application.koin.get<InstructorStudioRepository>())
            assertIs<StudioImageUploader>(application.koin.get<StudioImageUploader>())
            assertIs<RemoteInstructorProfileRepository>(application.koin.get<InstructorProfileRepository>())
            assertIs<DemoInstructorMyPageRepository>(application.koin.get<InstructorMyPageRepository>())
        } finally {
            application.koin.get<HttpClient>().close()
            application.koin.get<HttpClient>(named(OBJECT_STORAGE_HTTP_CLIENT)).close()
            application.close()
        }
    }

    private companion object {
        const val BASE_URL = "https://api.classitda.test/"
    }
}
