package com.classitda.di.mypage

import com.classitda.core.time.CurrentDateTimeProvider
import com.classitda.core.time.DeviceCurrentDateTimeProvider
import com.classitda.data.repository.mypage.FakeMyPassRepository
import com.classitda.domain.repository.mypage.MyPassRepository
import com.classitda.feature.student.mypage.holding.MyPassHoldingRequestArgs
import com.classitda.feature.student.mypage.holding.MyPassHoldingRequestViewModel
import com.classitda.feature.student.mypage.mypass.MyPassesViewModel
import com.classitda.feature.student.mypage.mypassdetail.MyPassDetailViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

internal val myPassModule =
    module {
        single<MyPassRepository> { FakeMyPassRepository() }
        single<CurrentDateTimeProvider> { DeviceCurrentDateTimeProvider }

        viewModel { MyPassesViewModel(repository = get()) }
        viewModel { parameters ->
            MyPassDetailViewModel(
                passId = parameters.get<String>(),
                repository = get(),
            )
        }
        viewModel { parameters ->
            MyPassHoldingRequestViewModel(
                args = parameters.get<MyPassHoldingRequestArgs>(),
                repository = get(),
                currentDateProvider = get(),
            )
        }
    }
