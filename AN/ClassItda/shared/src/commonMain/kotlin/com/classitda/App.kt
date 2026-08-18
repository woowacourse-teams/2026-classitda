package com.classitda

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.classitda.core.designsystem.AppTheme
import com.classitda.core.navigation.reservation.ReservationNavHost
import com.classitda.di.reservation.reservationModule
import org.koin.compose.KoinApplication
import org.koin.dsl.koinConfiguration

@Composable
@Preview
fun App() {
    KoinApplication(
        configuration =
            koinConfiguration {
                modules(reservationModule)
            },
    ) {
        AppTheme {
            ReservationNavHost()
        }
    }
}
