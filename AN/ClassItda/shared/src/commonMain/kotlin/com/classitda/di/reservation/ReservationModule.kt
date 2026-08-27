package com.classitda.di.reservation

import com.classitda.data.local.reservation.FakeReservationStore
import com.classitda.data.repository.classreservation.FakeClassReservationRepository
import com.classitda.data.repository.reservation.FakeReservationRepository
import com.classitda.data.repository.waitlist.FakeWaitlistReservationRepository
import com.classitda.domain.repository.classreservation.ClassReservationRepository
import com.classitda.domain.repository.reservation.ReservationRepository
import com.classitda.domain.repository.waitlist.WaitlistReservationRepository
import com.classitda.feature.student.reservation.ReservationViewModel
import com.classitda.feature.student.reservation.classreservation.ClassReservationViewModel
import com.classitda.feature.student.reservation.waitlist.WaitlistReservationViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

internal val reservationModule =
    module {
        single { FakeReservationStore() }
        single<ReservationRepository> { FakeReservationRepository(get()) }
        single<ClassReservationRepository> { FakeClassReservationRepository(get()) }
        single<WaitlistReservationRepository> { FakeWaitlistReservationRepository(get()) }

        viewModel { ReservationViewModel(get()) }
        viewModel { parameters -> ClassReservationViewModel(parameters.get(), parameters.get(), get()) }
        viewModel { parameters -> WaitlistReservationViewModel(parameters.get(), parameters.get(), get()) }
    }
