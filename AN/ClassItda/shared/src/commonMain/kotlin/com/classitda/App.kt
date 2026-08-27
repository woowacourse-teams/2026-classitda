package com.classitda

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.tooling.preview.Preview
import co.touchlab.kermit.Logger
import com.classitda.core.auth.AuthTokenStorage
import com.classitda.core.auth.InMemoryAuthTokenStorage
import com.classitda.core.auth.InMemoryWithdrawalStateStorage
import com.classitda.core.auth.WithdrawalStateStorage
import com.classitda.core.designsystem.AppTheme
import com.classitda.core.designsystem.ThemeType
import com.classitda.core.navigation.instructor.InstructorRootRoute
import com.classitda.core.network.ClassItdaApiConfig
import com.classitda.core.network.networkModule
import com.classitda.di.instructorFeatureModules
import com.classitda.di.signup.signupModule
import com.classitda.domain.repository.auth.signup.SignupRepository
import com.classitda.feature.auth.signup.SignupRoute
import com.classitda.feature.common.profile.WithdrawalPendingScreen
import kotlinx.coroutines.launch
import org.koin.compose.KoinApplication
import org.koin.compose.koinInject
import org.koin.dsl.koinConfiguration

@Composable
@Preview
fun App(
    tokenStorage: AuthTokenStorage = remember { InMemoryAuthTokenStorage() },
    withdrawalStateStorage: WithdrawalStateStorage = remember { InMemoryWithdrawalStateStorage() },
) {
    var appRoute by remember {
        mutableStateOf(
            when {
                withdrawalStateStorage.isPending() -> AppRoute.WithdrawalPending
                tokenStorage.read() == null -> AppRoute.Signup
                else -> AppRoute.Home
            },
        )
    }

    LaunchedEffect(appRoute) {
        Logger.d("AuthSession: appRoute=$appRoute, hasStoredToken=${tokenStorage.read() != null}")
    }

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
            val signupRepository = koinInject<SignupRepository>()
            val coroutineScope = rememberCoroutineScope()

            when (appRoute) {
                AppRoute.WithdrawalPending -> WithdrawalPendingScreen()

                AppRoute.Signup -> {
                    SignupRoute(
                        onSignupCompleted = { appRoute = AppRoute.Home },
                        onLoginCompleted = { appRoute = AppRoute.Home },
                    )
                }

                AppRoute.Home -> {
                    InstructorRootRoute(
                        onLogout = {
                            Logger.d("AuthSession: logout callback started")
                            coroutineScope.launch {
                                try {
                                    Logger.d("AuthSession: logout repository call started")
                                    signupRepository.logout()
                                    Logger.d("AuthSession: logout repository call completed")
                                } finally {
                                    Logger.d("AuthSession: switching to Google login screen")
                                    appRoute = AppRoute.Signup
                                }
                            }
                        },
                        onWithdrawalCompleted = {
                            Logger.d("AuthSession: withdrawal succeeded, saving pending state")
                            withdrawalStateStorage.markPending()
                            tokenStorage.clear()
                            appRoute = AppRoute.WithdrawalPending
                        },
                    )
                }
            }
        }
    }
}

private enum class AppRoute {
    Home,
    Signup,
    WithdrawalPending,
}
