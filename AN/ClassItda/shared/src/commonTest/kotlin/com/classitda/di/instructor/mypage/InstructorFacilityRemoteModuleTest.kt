package com.classitda.di.instructor.mypage

import com.classitda.core.network.createClassItdaHttpClient
import com.classitda.data.repository.instructor.mypage.DemoInstructorMyPageRepository
import com.classitda.data.repository.instructor.mypage.RemoteInstructorFacilityRepository
import com.classitda.domain.repository.instructor.mypage.FacilityImageUploader
import com.classitda.domain.repository.instructor.mypage.InstructorFacilityRepository
import com.classitda.domain.repository.instructor.mypage.InstructorMyPageRepository
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import org.koin.core.qualifier.named
import org.koin.dsl.koinApplication
import org.koin.dsl.module
import kotlin.test.Test
import kotlin.test.assertIs

class InstructorFacilityRemoteModuleTest {
    @Test
    fun `시설은 Remote binding이고 프로필 회원은 Demo binding이다`() {
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
                    instructorMyPageDemoModule,
                )
            }

        try {
            assertIs<RemoteInstructorFacilityRepository>(application.koin.get<InstructorFacilityRepository>())
            assertIs<FacilityImageUploader>(application.koin.get<FacilityImageUploader>())
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
