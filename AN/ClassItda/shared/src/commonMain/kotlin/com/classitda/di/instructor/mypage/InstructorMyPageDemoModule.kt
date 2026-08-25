package com.classitda.di.instructor.mypage

import com.classitda.data.repository.instructor.mypage.DemoInstructorMyPageRepository
import com.classitda.domain.repository.instructor.mypage.InstructorFacilityRepository
import com.classitda.domain.repository.instructor.mypage.InstructorMyPageRepository
import org.koin.dsl.module

/** App-only smoke fixture. Replace this binding when the production data layer is ready. */
internal val instructorMyPageDemoModule =
    module {
        single { DemoInstructorMyPageRepository() }
        single<InstructorMyPageRepository> { get<DemoInstructorMyPageRepository>() }
        single<InstructorFacilityRepository> { get<DemoInstructorMyPageRepository>() }
    }
