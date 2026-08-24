package com.classitda.di.instructor

import com.classitda.feature.instructor.home.InstructorHomeViewModel
import com.classitda.feature.instructor.schedule.InstructorScheduleViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

internal val instructorModule =
    module {
        includes(classManagementModule)
        viewModel { InstructorHomeViewModel(get()) }
        viewModel { InstructorScheduleViewModel(get()) }
    }
