package com.classitda

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.tooling.preview.Preview
import com.classitda.core.auth.AuthTokenStorage
import com.classitda.core.auth.InMemoryAuthTokenStorage
import com.classitda.core.designsystem.AppTheme
import com.classitda.core.designsystem.ThemeType
import com.classitda.core.navigation.instructor.InstructorRootRoute
import com.classitda.core.network.ClassItdaApiConfig
import com.classitda.core.network.networkModule
import com.classitda.di.instructorFeatureModules
import com.classitda.di.signup.signupModule
import com.classitda.feature.auth.signup.SignupRoute
import org.koin.compose.KoinApplication
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
                    instructorFeatureModules,
                )
            },
    ) {
        AppTheme(theme = ThemeType.INSTRUCTOR) {
            if (showSignup) {
                SignupRoute(onSignupCompleted = { showSignup = false })
            } else {
                InstructorRootRoute()
            }
        }
    }
}
