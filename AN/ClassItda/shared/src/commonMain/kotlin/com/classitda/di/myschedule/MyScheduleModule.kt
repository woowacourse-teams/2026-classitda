package com.classitda.di.myschedule

import com.classitda.data.repository.student.myschedule.FakeMyScheduleRepository
import com.classitda.data.repository.student.myschedule.MY_SCHEDULE_FAKE_CURRENT_TIME
import com.classitda.data.repository.student.myschedule.createDefaultMyScheduleFakeRepository
import com.classitda.domain.model.student.myschedule.ReservationId
import com.classitda.domain.model.student.myschedule.WaitlistId
import com.classitda.domain.repository.student.myschedule.MyScheduleRepository
import com.classitda.feature.student.myschedule.MyScheduleViewModel
import com.classitda.feature.student.myschedule.ReservationDetailViewModel
import com.classitda.feature.student.myschedule.WaitlistDetailViewModel
import com.classitda.feature.student.myschedule.mapper.CurrentTimeProvider
import com.classitda.feature.student.myschedule.mapper.MyScheduleDisplayLocale
import com.classitda.feature.student.myschedule.mapper.MyScheduleUiMapper
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

internal val myScheduleDemoModule =
    module {
        single { createDefaultMyScheduleFakeRepository() }
        single<MyScheduleRepository> {
            get<FakeMyScheduleRepository>()
        }
        single<CurrentTimeProvider> {
            CurrentTimeProvider { MY_SCHEDULE_FAKE_CURRENT_TIME }
        }
        single {
            MyScheduleUiMapper(
                locale = MyScheduleDisplayLocale.KOREAN,
                currentTimeProvider = get(),
            )
        }

        viewModel { MyScheduleViewModel(repository = get(), mapper = get()) }
        viewModel { parameters ->
            ReservationDetailViewModel(
                reservationId = parameters.get<ReservationId>(),
                cancellationDeadlineHoursBeforeStart = 4,
                repository = get(),
                mapper = get(),
            )
        }
        viewModel { parameters ->
            WaitlistDetailViewModel(
                waitlistId = parameters.get<WaitlistId>(),
                repository = get(),
                mapper = get(),
            )
        }
    }
