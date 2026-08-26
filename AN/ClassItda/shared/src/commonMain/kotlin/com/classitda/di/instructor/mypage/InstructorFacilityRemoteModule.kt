package com.classitda.di.instructor.mypage

import com.classitda.core.network.createObjectStorageHttpClient
import com.classitda.core.platform.createFacilityImageBinaryReader
import com.classitda.data.remote.instructor.mypage.facility.FacilityImageUploadApi
import com.classitda.data.remote.instructor.mypage.facility.ObjectStorageUploadDataSource
import com.classitda.data.remote.instructor.mypage.facility.StudioApi
import com.classitda.data.remote.instructor.mypage.facility.StudioRemoteDataSource
import com.classitda.data.repository.instructor.mypage.RemoteFacilityImageUploader
import com.classitda.data.repository.instructor.mypage.RemoteInstructorFacilityRepository
import com.classitda.domain.repository.instructor.mypage.FacilityImageUploader
import com.classitda.domain.repository.instructor.mypage.InstructorFacilityRepository
import io.ktor.client.HttpClient
import org.koin.core.qualifier.named
import org.koin.dsl.module

internal const val OBJECT_STORAGE_HTTP_CLIENT = "facility-object-storage-http-client"

internal val instructorFacilityRemoteModule =
    module {
        single { StudioApi(get<HttpClient>()) }
        single { StudioRemoteDataSource(get()) }
        single { FacilityImageUploadApi(get<HttpClient>()) }
        single(named(OBJECT_STORAGE_HTTP_CLIENT)) { createObjectStorageHttpClient() }
        single {
            ObjectStorageUploadDataSource(
                client = get(named(OBJECT_STORAGE_HTTP_CLIENT)),
                binaryReader = createFacilityImageBinaryReader(),
            )
        }
        single<FacilityImageUploader> {
            RemoteFacilityImageUploader(
                uploadApi = get(),
                objectStorage = get(),
            )
        }
        single<InstructorFacilityRepository> {
            RemoteInstructorFacilityRepository(
                remoteDataSource = get(),
                imageUploader = get(),
            )
        }
    }
