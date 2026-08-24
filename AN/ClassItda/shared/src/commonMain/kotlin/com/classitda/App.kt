package com.classitda

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.tooling.preview.Preview
import com.classitda.core.auth.AuthTokenStorage
import com.classitda.core.auth.InMemoryAuthTokenStorage
import com.classitda.core.designsystem.AppTheme
import com.classitda.core.designsystem.ThemeType
import com.classitda.core.navigation.student.StudentRootRoute
import com.classitda.core.network.ClassItdaApiConfig
import com.classitda.core.network.networkModule
import com.classitda.di.home.homeModule
import com.classitda.di.instructor.instructorModule
import com.classitda.di.mypage.myPageModule
import com.classitda.di.myschedule.myScheduleModule
import com.classitda.di.reservation.reservationModule
import com.classitda.di.signup.signupModule
import com.classitda.domain.repository.auth.signup.SignupRepository
import com.classitda.feature.auth.signup.SignupRoute
import kotlinx.coroutines.launch
import org.koin.compose.KoinApplication
import org.koin.compose.koinInject
import org.koin.dsl.koinConfiguration

@Composable
@Preview
fun App(tokenStorage: AuthTokenStorage = remember { InMemoryAuthTokenStorage() }) {
    var showSignup by remember { mutableStateOf(tokenStorage.read() == null) }

    KoinApplication(
        configuration =
            koinConfiguration {
                modules(
                    networkModule(
                        com.classitda.core.network
                            .NetworkConfig(ClassItdaApiConfig.BASE_URL),
                        tokenStorage,
                    ),
                    signupModule(tokenStorage),
                    homeModule,
                    instructorModule,
                    reservationModule,
                    myScheduleModule,
                    myPageModule,
                )
            },
    ) {
        val signupRepository = koinInject<SignupRepository>()
        val coroutineScope = rememberCoroutineScope()
        AppTheme(theme = ThemeType.STUDENT) {
            if (showSignup) {
                SignupRoute(onSignupCompleted = { showSignup = false })
            } else {
                StudentRootRoute(
                    onLogout = {
                        coroutineScope.launch {
                            signupRepository.logout()
                            showSignup = true
                        }
                    },
                )
            }
        }
    }
}
