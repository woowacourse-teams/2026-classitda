package com.classitda.feature.student.myschedule

import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import com.classitda.di.myschedule.myScheduleModule
import com.classitda.domain.model.student.myschedule.ReservationId
import com.classitda.domain.model.student.myschedule.WaitlistId
import kotlinx.serialization.Serializable
import org.koin.compose.KoinApplication
import org.koin.dsl.koinConfiguration

@Serializable
private data class MyScheduleListDestination(
    val version: Int,
)

@Serializable
private data class ReservationDetailDestination(
    val reservationId: String,
)

@Serializable
private data class WaitlistDetailDestination(
    val waitlistId: String,
)

@Composable
internal fun MyScheduleFlowRoute(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
    onBookAnotherClass: (() -> Unit)? = null,
    bottomBar: @Composable () -> Unit = {},
) {
    var listVersion by remember { mutableIntStateOf(0) }

    fun navigateToFreshList() {
        listVersion += 1
        navController.navigate(MyScheduleListDestination(version = listVersion)) {
            popUpTo(navController.graph.id) {
                inclusive = true
            }
        }
    }

    val bookAnotherClass = onBookAnotherClass ?: ::navigateToFreshList

    NavHost(
        navController = navController,
        startDestination = MyScheduleListDestination(version = listVersion),
        modifier =
            modifier
                .statusBarsPadding()
                .navigationBarsPadding()
                .clipToBounds(),
    ) {
        composable<MyScheduleListDestination> { backStackEntry ->
            val destination = backStackEntry.toRoute<MyScheduleListDestination>()
            Scaffold(bottomBar = bottomBar) { innerPadding ->
                MyScheduleRoute(
                    viewModelKey = "my-schedule-list-${destination.version}",
                    modifier = Modifier.padding(innerPadding),
                    onOpenReservation = { reservationId ->
                        navController.navigate(
                            ReservationDetailDestination(reservationId = reservationId.value),
                        )
                    },
                    onOpenWaitlist = { waitlistId ->
                        navController.navigate(
                            WaitlistDetailDestination(waitlistId = waitlistId.value),
                        )
                    },
                )
            }
        }

        composable<ReservationDetailDestination> { backStackEntry ->
            val destination = backStackEntry.toRoute<ReservationDetailDestination>()
            ReservationDetailRoute(
                reservationId = ReservationId(destination.reservationId),
                onBack = navController::popBackStack,
                onBookAnotherClass = bookAnotherClass,
                onReturnToList = ::navigateToFreshList,
            )
        }

        composable<WaitlistDetailDestination> { backStackEntry ->
            val destination = backStackEntry.toRoute<WaitlistDetailDestination>()
            WaitlistDetailRoute(
                waitlistId = WaitlistId(destination.waitlistId),
                onBack = navController::popBackStack,
                onBookAnotherClass = bookAnotherClass,
                onReturnToList = ::navigateToFreshList,
            )
        }
    }
}

@Preview(
    name = "F01-F10 Fake flow / Student",
    group = "Screen/MySchedule/Flow",
    showBackground = true,
    locale = "ko",
    widthDp = 390,
    heightDp = 844,
)
@Composable
private fun MyScheduleFlowRoutePreview() {
    KoinApplication(
        configuration =
            koinConfiguration {
                modules(myScheduleModule)
            },
    ) {
        AppTheme(theme = ThemeType.STUDENT) {
            MyScheduleFlowRoute()
        }
    }
}
