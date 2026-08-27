package com.classitda.di.instructor.mypage

import com.classitda.data.repository.instructor.mypage.DemoInstructorMyPageRepository
import com.classitda.domain.repository.instructor.mypage.InstructorMyPageRepository
import org.koin.dsl.module

/** Keeps profile smoke data while the studio and membership features use remote repositories. */
internal val instructorMyPageDemoModule =
    module {
        includes(instructorStudioRemoteModule)
        single { DemoInstructorMyPageRepository() }
        single<InstructorMyPageRepository> { get<DemoInstructorMyPageRepository>() }
    }
