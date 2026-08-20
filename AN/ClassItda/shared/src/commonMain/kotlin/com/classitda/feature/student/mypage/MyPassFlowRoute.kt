package com.classitda.feature.student.mypage

import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.classitda.core.designsystem.AppTheme
import com.classitda.core.designsystem.ThemeType
import com.classitda.di.mypage.myPassModule
import com.classitda.feature.student.mypage.holding.MyPassHoldingCompletedScreen
import com.classitda.feature.student.mypage.holding.MyPassHoldingRequestRoute
import com.classitda.feature.student.mypage.holding.model.MyPassHoldingCompletedUiModel
import com.classitda.feature.student.mypage.mypass.MyPassRoute
import com.classitda.feature.student.mypage.mypassdetail.MyPassDetailRoute
import kotlinx.datetime.LocalDate
import kotlinx.serialization.Serializable
import org.koin.compose.KoinApplication
import org.koin.dsl.koinConfiguration

@Serializable
private data object MyPassListDestination

@Serializable
private data class MyPassDetailDestination(
    val passId: String,
)

@Serializable
private data class MyPassHoldingRequestDestination(
    val passId: String,
    val passName: String,
    val currentExpireDateIso: String,
)

@Serializable
private data class MyPassHoldingCompletedDestination(
    val requestPeriodLabel: String,
    val totalHoldingDaysLabel: String,
    val currentExpireDateLabel: String,
    val newExpireDateLabel: String,
)

@Composable
internal fun MyPassFlowRoute(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
    onNavigateUp: (() -> Unit)? = null,
    bottomBar: @Composable () -> Unit = {},
) {
    NavHost(
        navController = navController,
        startDestination = MyPassListDestination,
        modifier =
            modifier
                .statusBarsPadding()
                .navigationBarsPadding()
                .clipToBounds(),
    ) {
        composable<MyPassListDestination> {
            Scaffold(bottomBar = bottomBar) { innerPadding ->
                MyPassRoute(
                    modifier = Modifier.padding(innerPadding),
                    onNavigateBack = { onNavigateUp?.invoke() },
                    onPassClick = { passId ->
                        navController.navigate(MyPassDetailDestination(passId = passId))
                    },
                )
            }
        }

        composable<MyPassDetailDestination> { backStackEntry ->
            val destination = backStackEntry.toRoute<MyPassDetailDestination>()
            MyPassDetailRoute(
                passId = destination.passId,
                onNavigateBack = navController::popBackStack,
                onHoldRequestClick = { passId, passName, currentExpireDate ->
                    navController.navigate(
                        MyPassHoldingRequestDestination(
                            passId = passId,
                            passName = passName,
                            currentExpireDateIso = currentExpireDate.toString(),
                        ),
                    )
                },
            )
        }

        composable<MyPassHoldingRequestDestination> { backStackEntry ->
            val destination = backStackEntry.toRoute<MyPassHoldingRequestDestination>()
            MyPassHoldingRequestRoute(
                passId = destination.passId,
                passName = destination.passName,
                currentExpireDate = LocalDate.parse(destination.currentExpireDateIso),
                onNavigateBack = navController::popBackStack,
                onCompleted = { completed ->
                    navController.navigate(completed.toDestination()) {
                        popUpTo<MyPassDetailDestination> {
                            inclusive = false
                        }
                    }
                },
            )
        }

        composable<MyPassHoldingCompletedDestination> { backStackEntry ->
            val destination = backStackEntry.toRoute<MyPassHoldingCompletedDestination>()
            MyPassHoldingCompletedScreen(
                uiModel = destination.toUiModel(),
                onReturnToDetailClick = navController::popBackStack,
            )
        }
    }
}

private fun MyPassHoldingCompletedUiModel.toDestination() =
    MyPassHoldingCompletedDestination(
        requestPeriodLabel = requestPeriodLabel,
        totalHoldingDaysLabel = totalHoldingDaysLabel,
        currentExpireDateLabel = currentExpireDateLabel,
        newExpireDateLabel = newExpireDateLabel,
    )

private fun MyPassHoldingCompletedDestination.toUiModel() =
    MyPassHoldingCompletedUiModel(
        requestPeriodLabel = requestPeriodLabel,
        totalHoldingDaysLabel = totalHoldingDaysLabel,
        currentExpireDateLabel = currentExpireDateLabel,
        newExpireDateLabel = newExpireDateLabel,
    )

@Preview(
    name = "MyPass flow / Student",
    group = "Screen/MyPass/Flow",
    showBackground = true,
    locale = "ko",
    widthDp = 390,
    heightDp = 844,
)
@Composable
private fun MyPassFlowRoutePreview() {
    KoinApplication(
        configuration =
            koinConfiguration {
                modules(myPassModule)
            },
    ) {
        AppTheme(theme = ThemeType.STUDENT) {
            MyPassFlowRoute()
        }
    }
}
