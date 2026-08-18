package com.classitda.di.home

import com.classitda.data.repository.home.FakeNoticeRepository
import com.classitda.data.repository.home.FakePassRepository
import com.classitda.data.repository.home.FakeReservationRepository
import com.classitda.domain.repository.home.NoticeRepository
import com.classitda.domain.repository.home.PassRepository
import com.classitda.domain.repository.home.ReservationRepository
import com.classitda.feature.student.home.HomeViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

internal val homeModule =
    module {
        single<ReservationRepository> { FakeReservationRepository() }
        single<PassRepository> { FakePassRepository() }
        single<NoticeRepository> { FakeNoticeRepository() }

        viewModel {
            HomeViewModel(
                reservationRepository = get(),
                passRepository = get(),
                noticeRepository = get(),
            )
        }
    }
