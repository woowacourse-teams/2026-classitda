package com.classitda.di.instructor.mypage

import com.classitda.data.repository.instructor.mypage.DemoInstructorMyPageRepository
import com.classitda.domain.repository.instructor.mypage.InstructorMyPageRepository
import org.koin.dsl.module

/** Keeps profile/member smoke data while the studio feature uses its remote repository. */
internal val instructorMyPageDemoModule =
    module {
        includes(instructorStudioRemoteModule)
        single { DemoInstructorMyPageRepository() }
        single<InstructorMyPageRepository> { get<DemoInstructorMyPageRepository>() }
    }
