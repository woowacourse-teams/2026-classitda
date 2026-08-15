package com.classitda.feature.student.reservation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.classitda.feature.student.reservation.classreservation.ClassReservationRoute
import com.classitda.feature.student.reservation.complete.ReservationCompleteRoute
import com.classitda.feature.student.reservation.complete.WaitlistCompleteRoute
import com.classitda.feature.student.reservation.waitlist.WaitlistReservationRoute
import kotlinx.serialization.Serializable

@Serializable
private data object ReservationDestination

@Serializable
private data class ClassReservationDestination(
    val classId: String,
)

@Serializable
private data class WaitlistReservationDestination(
    val classId: String,
)

@Serializable
private data class ReservationCompleteDestination(
    val classId: String,
)

@Serializable
private data class WaitlistCompleteDestination(
    val classId: String,
)

@Composable
internal fun ReservationNavHost(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
) {
    fun navigateHome() {
        navController.navigate(ReservationDestination) {
            popUpTo(navController.graph.id) {
                inclusive = true
            }
        }
    }

    NavHost(
        navController = navController,
        startDestination = ReservationDestination,
        modifier = modifier,
    ) {
        composable<ReservationDestination> {
            ReservationRoute(
                onClassReservationClick = { classId ->
                    navController.navigate(ClassReservationDestination(classId))
                },
                onWaitlistReservationClick = { classId ->
                    navController.navigate(WaitlistReservationDestination(classId))
                },
            )
        }

        composable<ClassReservationDestination> { backStackEntry ->
            val destination = backStackEntry.toRoute<ClassReservationDestination>()
            ClassReservationRoute(
                classId = destination.classId,
                onBackClick = navController::popBackStack,
                onReservationComplete = { classId ->
                    navController.navigate(ReservationCompleteDestination(classId))
                },
            )
        }

        composable<WaitlistReservationDestination> { backStackEntry ->
            val destination = backStackEntry.toRoute<WaitlistReservationDestination>()
            WaitlistReservationRoute(
                classId = destination.classId,
                onBackClick = navController::popBackStack,
                onWaitlistComplete = { classId ->
                    navController.navigate(WaitlistCompleteDestination(classId))
                },
            )
        }

        composable<ReservationCompleteDestination> { backStackEntry ->
            val destination = backStackEntry.toRoute<ReservationCompleteDestination>()
            ReservationCompleteRoute(
                classId = destination.classId,
                onCloseClick = ::navigateHome,
                onScheduleClick = ::navigateHome,
                onHomeClick = ::navigateHome,
            )
        }

        composable<WaitlistCompleteDestination> { backStackEntry ->
            val destination = backStackEntry.toRoute<WaitlistCompleteDestination>()
            WaitlistCompleteRoute(
                classId = destination.classId,
                onCloseClick = ::navigateHome,
                onScheduleClick = ::navigateHome,
                onHomeClick = ::navigateHome,
            )
        }
    }
}
