package com.classitda

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.classitda.core.designsystem.AppTheme
import com.classitda.core.designsystem.ThemeType
import com.classitda.core.navigation.student.StudentRootRoute
import com.classitda.di.home.homeModule
import com.classitda.di.myschedule.myScheduleModule
import com.classitda.di.reservation.reservationModule
import org.koin.compose.KoinApplication
import org.koin.dsl.koinConfiguration

@Composable
@Preview
fun App() {
    KoinApplication(
        configuration =
            koinConfiguration {
                modules(homeModule, reservationModule, myScheduleModule)
            },
    ) {
        AppTheme(theme = ThemeType.STUDENT) {
            StudentRootRoute()
        }
    }
}
