package com.classitda.di.instructor

import com.classitda.data.remote.instructor.member.InstructorMemberApi
import com.classitda.data.repository.instructor.member.RemoteInstructorMemberRepository
import com.classitda.domain.repository.instructor.member.InstructorMemberRepository
import org.koin.dsl.module

internal val instructorMemberModule =
    module {
        single { InstructorMemberApi(get()) }
        single<InstructorMemberRepository> { RemoteInstructorMemberRepository(get()) }
    }
