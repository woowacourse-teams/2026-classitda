package com.classitda.di.mypage

import com.classitda.data.repository.mypage.FakeMyPassRepository
import com.classitda.domain.repository.mypage.MyPassRepository
import com.classitda.feature.student.mypage.mypass.MyPassesViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

internal val myPassModule =
    module {
        single<MyPassRepository> { FakeMyPassRepository() }

        viewModel { MyPassesViewModel(repository = get()) }
    }
