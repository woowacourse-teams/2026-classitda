package com.classitda.di.instructor

import com.classitda.data.remote.member.MemberApi
import com.classitda.data.repository.member.RemoteMemberRepository
import com.classitda.domain.repository.member.MemberRepository
import com.classitda.feature.instructor.home.InstructorHomeViewModel
import com.classitda.feature.instructor.schedule.InstructorScheduleViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

internal val instructorModule =
    module {
        includes(classManagementModule, instructorMemberModule, studioModule, instructorSessionModule)
        single { MemberApi(get()) }
        single<MemberRepository> { RemoteMemberRepository(get()) }
        viewModel { InstructorHomeViewModel(get(), get(), get()) }
        viewModel { InstructorScheduleViewModel(get(), get()) }
    }
