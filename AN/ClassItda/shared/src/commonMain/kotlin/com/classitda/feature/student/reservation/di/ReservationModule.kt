package com.classitda.feature.student.reservation.di

import com.classitda.feature.student.reservation.ReservationViewModel
import com.classitda.feature.student.reservation.classreservation.ClassReservationViewModel
import com.classitda.feature.student.reservation.data.repository.classreservation.FakeClassReservationRepository
import com.classitda.feature.student.reservation.data.repository.reservation.FakeReservationRepository
import com.classitda.feature.student.reservation.data.repository.waitlist.FakeWaitlistReservationRepository
import com.classitda.feature.student.reservation.domain.repository.classreservation.ClassReservationRepository
import com.classitda.feature.student.reservation.domain.repository.reservation.ReservationRepository
import com.classitda.feature.student.reservation.domain.repository.waitlist.WaitlistReservationRepository
import com.classitda.feature.student.reservation.waitlist.WaitlistReservationViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

internal val reservationModule =
    module {
        single<ReservationRepository> { FakeReservationRepository() }
        single<ClassReservationRepository> { FakeClassReservationRepository() }
        single<WaitlistReservationRepository> { FakeWaitlistReservationRepository() }

        viewModel { ReservationViewModel(get()) }
        viewModel { parameters -> ClassReservationViewModel(parameters.get(), get()) }
        viewModel { parameters -> WaitlistReservationViewModel(parameters.get(), get()) }
    }
