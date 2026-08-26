package com.classitda.di.instructor.mypage

import com.classitda.core.network.createObjectStorageHttpClient
import com.classitda.core.platform.createStudioImageBinaryReader
import com.classitda.data.remote.instructor.mypage.studio.ObjectStorageUploadDataSource
import com.classitda.data.remote.instructor.mypage.studio.StudioApi
import com.classitda.data.remote.instructor.mypage.studio.StudioImageUploadApi
import com.classitda.data.remote.instructor.mypage.studio.StudioRemoteDataSource
import com.classitda.data.repository.instructor.mypage.RemoteInstructorStudioRepository
import com.classitda.data.repository.instructor.mypage.RemoteStudioImageUploader
import com.classitda.domain.repository.instructor.mypage.InstructorStudioRepository
import com.classitda.domain.repository.instructor.mypage.StudioImageUploader
import io.ktor.client.HttpClient
import org.koin.core.qualifier.named
import org.koin.dsl.module

internal const val OBJECT_STORAGE_HTTP_CLIENT = "studio-object-storage-http-client"

internal val instructorStudioRemoteModule =
    module {
        single { StudioApi(get<HttpClient>()) }
        single { StudioRemoteDataSource(get()) }
        single { StudioImageUploadApi(get<HttpClient>()) }
        single(named(OBJECT_STORAGE_HTTP_CLIENT)) { createObjectStorageHttpClient() }
        single {
            ObjectStorageUploadDataSource(
                client = get(named(OBJECT_STORAGE_HTTP_CLIENT)),
                binaryReader = createStudioImageBinaryReader(),
            )
        }
        single<StudioImageUploader> {
            RemoteStudioImageUploader(
                uploadApi = get(),
                objectStorage = get(),
            )
        }
        single<InstructorStudioRepository> {
            RemoteInstructorStudioRepository(
                remoteDataSource = get(),
                imageUploader = get(),
            )
        }
    }
