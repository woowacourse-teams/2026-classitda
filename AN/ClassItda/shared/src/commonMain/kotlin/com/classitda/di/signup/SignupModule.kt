package com.classitda.di.signup

import com.classitda.data.remote.auth.signup.SignupApi
import com.classitda.data.repository.auth.signup.RemoteSignupRepository
import com.classitda.domain.repository.auth.signup.SignupRepository
import com.classitda.feature.auth.signup.SignupViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

internal val signupModule =
    module {
        single { SignupApi(get()) }
        single<SignupRepository> { RemoteSignupRepository(get()) }
        viewModel { SignupViewModel(get()) }
    }
