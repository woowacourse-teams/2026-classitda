package com.classitda.di.instructor

import com.classitda.data.remote.instructor.session.InstructorSessionApi
import com.classitda.data.repository.instructor.session.RemoteInstructorSessionRepository
import com.classitda.domain.repository.instructor.session.InstructorSessionRepository
import org.koin.dsl.module

internal val instructorSessionModule =
    module {
        single { InstructorSessionApi(get()) }
        single<InstructorSessionRepository> { RemoteInstructorSessionRepository(get()) }
    }
