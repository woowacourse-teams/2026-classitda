package com.classitda.di.instructor.mypage

import com.classitda.feature.instructor.mypage.FacilityDetailViewModel
import com.classitda.feature.instructor.mypage.FacilityEditViewModel
import com.classitda.feature.instructor.mypage.FacilityManagementViewModel
import com.classitda.feature.instructor.mypage.FacilityRegistrationViewModel
import com.classitda.feature.instructor.mypage.InstructorMyPageViewModel
import com.classitda.feature.instructor.mypage.InstructorPhoneNumberChangeViewModel
import com.classitda.feature.instructor.mypage.InstructorProfileEditViewModel
import com.classitda.feature.instructor.mypage.InstructorProfileViewModel
import com.classitda.feature.instructor.mypage.MemberManagementViewModel
import com.classitda.feature.instructor.mypage.MemberRegistrationViewModel
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
        viewModel { FacilityManagementViewModel(get()) }
        viewModel { FacilityRegistrationViewModel(get()) }
        viewModel { parameters -> FacilityDetailViewModel(get(), parameters.get()) }
        viewModel { parameters -> FacilityEditViewModel(get(), parameters.get()) }
    }
