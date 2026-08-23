package com.classitda

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.tooling.preview.Preview
import com.classitda.core.designsystem.AppTheme
import com.classitda.core.designsystem.ThemeType
import com.classitda.core.navigation.student.StudentRootRoute
import com.classitda.di.home.homeModule
import com.classitda.di.mypage.myPageModule
import com.classitda.di.myschedule.myScheduleModule
import com.classitda.di.reservation.reservationModule
import com.classitda.feature.auth.signup.SignupRoute
import org.koin.compose.KoinApplication
import org.koin.dsl.koinConfiguration

@Composable
@Preview
fun App() {
    var showSignup by remember { mutableStateOf(true) }

    KoinApplication(
        configuration =
            koinConfiguration {
                modules(homeModule, reservationModule, myScheduleModule, myPageModule)
            },
    ) {
        AppTheme(theme = ThemeType.STUDENT) {
            if (showSignup) {
                SignupRoute(onSignupCompleted = { showSignup = false })
            } else {
                StudentRootRoute(onLogout = { showSignup = true })
            }
        }
    }
}
