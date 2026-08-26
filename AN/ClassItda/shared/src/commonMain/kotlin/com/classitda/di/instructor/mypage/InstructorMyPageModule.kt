package com.classitda.di.instructor.mypage

import com.classitda.feature.instructor.mypage.InstructorMyPageViewModel
import com.classitda.feature.instructor.mypage.member.MemberEditViewModel
import com.classitda.feature.instructor.mypage.member.MemberManagementViewModel
import com.classitda.feature.instructor.mypage.member.MemberRegistrationViewModel
import com.classitda.feature.instructor.mypage.profile.InstructorPhoneNumberChangeViewModel
import com.classitda.feature.instructor.mypage.profile.InstructorProfileEditViewModel
import com.classitda.feature.instructor.mypage.profile.InstructorProfileViewModel
import com.classitda.feature.instructor.mypage.studio.StudioDetailViewModel
import com.classitda.feature.instructor.mypage.studio.StudioEditViewModel
import com.classitda.feature.instructor.mypage.studio.StudioManagementViewModel
import com.classitda.feature.instructor.mypage.studio.StudioRegistrationViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

/**
 * App assembly owns the repository binding. This feature module only declares the
 * ViewModel graph so a host can provide its InstructorMyPageRepository implementation.
 */
internal val instructorMyPageModule =
    module {
        viewModel { InstructorMyPageViewModel(get()) }
        viewModel { InstructorProfileViewModel(get()) }
        viewModel { InstructorProfileEditViewModel(get()) }
        viewModel { parameters -> InstructorPhoneNumberChangeViewModel(get(), parameters.get()) }
        viewModel { MemberManagementViewModel(get()) }
        viewModel { MemberRegistrationViewModel(get()) }
        viewModel { parameters -> MemberEditViewModel(get(), parameters.get()) }
        viewModel { StudioManagementViewModel(get()) }
        viewModel { StudioRegistrationViewModel(get()) }
        viewModel { parameters -> StudioDetailViewModel(get(), parameters.get()) }
        viewModel { parameters -> StudioEditViewModel(get(), parameters.get()) }
    }
