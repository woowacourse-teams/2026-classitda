package com.classitda.di.instructor

import com.classitda.core.studio.InstructorStudioContext
import com.classitda.data.remote.studio.StudioApi
import com.classitda.data.repository.studio.RemoteStudioRepository
import com.classitda.domain.repository.studio.StudioRepository
import org.koin.dsl.module

internal val studioModule =
    module {
        single { StudioApi(get()) }
        single<StudioRepository> { RemoteStudioRepository(get()) }
        single { InstructorStudioContext(get()) }
    }
