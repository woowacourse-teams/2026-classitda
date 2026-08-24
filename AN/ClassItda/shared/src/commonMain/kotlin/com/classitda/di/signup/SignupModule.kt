package com.classitda.di.signup

import com.classitda.core.auth.AuthTokenStorage
import com.classitda.data.remote.auth.signup.SignupApi
import com.classitda.data.repository.auth.signup.RemoteSignupRepository
import com.classitda.domain.repository.auth.signup.SignupRepository
import com.classitda.feature.auth.signup.SignupViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

internal fun signupModule(tokenStorage: AuthTokenStorage) =
    module {
        single { SignupApi(get()) }
        single<SignupRepository> { RemoteSignupRepository(get(), tokenStorage) }
        viewModel { SignupViewModel(get()) }
    }
