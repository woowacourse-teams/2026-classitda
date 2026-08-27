package com.classitda.di.instructor

import com.classitda.data.remote.member.MemberApi
import com.classitda.data.repository.auth.RemoteInstructorAccountLifecycleRepository
import com.classitda.data.repository.instructor.mypage.NetworkFirstInstructorMyPageRepository
import com.classitda.data.repository.instructor.mypage.RemoteInstructorProfileRepository
import com.classitda.data.repository.member.RemoteMemberRepository
import com.classitda.domain.repository.auth.InstructorAccountLifecycleRepository
import com.classitda.domain.repository.instructor.mypage.InstructorMyPageSummaryRepository
import com.classitda.domain.repository.instructor.mypage.InstructorProfileRepository
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
        single<InstructorProfileRepository> { RemoteInstructorProfileRepository(get()) }
        single<InstructorMyPageSummaryRepository> {
            NetworkFirstInstructorMyPageRepository(
                remoteProfileRepository = get(),
                cache = get(),
            )
        }
        single<InstructorAccountLifecycleRepository> { RemoteInstructorAccountLifecycleRepository(get(), get()) }
        viewModel { InstructorHomeViewModel(get(), get(), get()) }
        viewModel { InstructorScheduleViewModel(get(), get()) }
    }
