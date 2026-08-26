package com.classitda.di

import com.classitda.di.home.homeModule
import com.classitda.di.instructor.instructorModule
import com.classitda.di.instructor.mypage.instructorMyPageDemoModule
import com.classitda.di.instructor.mypage.instructorMyPageModule
import com.classitda.di.mypage.myPageModule
import com.classitda.di.myschedule.myScheduleModule
import com.classitda.di.reservation.reservationModule
import org.koin.dsl.module

internal val instructorFeatureModules =
    module {
        includes(instructorModule, instructorMyPageModule, instructorMyPageDemoModule)
    }

internal val studentFeatureModules =
    module {
        includes(homeModule, reservationModule, myScheduleModule, myPageModule)
    }
