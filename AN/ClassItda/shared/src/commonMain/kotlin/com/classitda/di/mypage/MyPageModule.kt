package com.classitda.di.mypage

import com.classitda.data.repository.student.mypage.DemoMyPageRepository
import com.classitda.domain.repository.student.mypage.MyPageRepository
import com.classitda.feature.student.mypage.ConnectedFacilitiesViewModel
import com.classitda.feature.student.mypage.MyPageViewModel
import com.classitda.feature.student.mypage.NotificationSettingsViewModel
import com.classitda.feature.student.mypage.PhoneNumberChangeViewModel
import com.classitda.feature.student.mypage.ProfileEditViewModel
import com.classitda.feature.student.mypage.ProfileViewModel
import com.classitda.feature.student.mypage.mapper.MyPageUiMapper
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

internal fun myPageDemoModule(isInstructorSignupBannerVisible: Boolean = true) =
    module {
        single { DemoMyPageRepository(isInstructorSignupBannerVisible = isInstructorSignupBannerVisible) }
        single<MyPageRepository> { get<DemoMyPageRepository>() }
        single { MyPageUiMapper() }

        viewModel { MyPageViewModel(repository = get(), mapper = get()) }
        viewModel { ProfileViewModel(repository = get(), mapper = get()) }
        viewModel { ProfileEditViewModel(repository = get(), mapper = get()) }
        viewModel { ConnectedFacilitiesViewModel(repository = get(), mapper = get()) }
        viewModel { parameters ->
            PhoneNumberChangeViewModel(
                repository = get(),
                initialPhoneNumber = parameters.get(),
            )
        }
        viewModel {
            NotificationSettingsViewModel(
                repository = get(),
                mapper = get(),
            )
        }
    }

internal val myPageModule = myPageDemoModule()
