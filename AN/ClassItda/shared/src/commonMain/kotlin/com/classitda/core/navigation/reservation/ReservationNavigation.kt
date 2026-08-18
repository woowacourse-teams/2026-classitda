package com.classitda.core.navigation.reservation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.classitda.feature.student.reservation.ReservationRoute
import com.classitda.feature.student.reservation.classreservation.ClassReservationRoute
import com.classitda.feature.student.reservation.complete.ReservationCompleteRoute
import com.classitda.feature.student.reservation.complete.WaitlistCompleteRoute
import com.classitda.feature.student.reservation.error.ReservationRequestErrorScreen
import com.classitda.feature.student.reservation.waitlist.WaitlistReservationRoute
import kotlinx.serialization.Serializable

@Serializable
private data object ReservationDestination

@Serializable
private data class ClassReservationDestination(
    val classId: String,
    val passId: String,
)

@Serializable
private data class WaitlistReservationDestination(
    val classId: String,
    val passId: String,
)

@Serializable
private data class ReservationCompleteDestination(
    val classId: String,
    val passId: String,
)

@Serializable
private data class WaitlistCompleteDestination(
    val classId: String,
    val passId: String,
)

@Serializable
private data class ReservationRequestErrorDestination(
    val title: String,
    val message: String,
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
                onClassReservationClick = { classId, passId ->
                    navController.navigate(ClassReservationDestination(classId, passId))
                },
                onWaitlistReservationClick = { classId, passId ->
                    navController.navigate(WaitlistReservationDestination(classId, passId))
                },
            )
        }

        composable<ClassReservationDestination> { backStackEntry ->
            val destination = backStackEntry.toRoute<ClassReservationDestination>()
            ClassReservationRoute(
                classId = destination.classId,
                initialPassId = destination.passId,
                onBackClick = navController::popBackStack,
                onReservationComplete = { classId, passId ->
                    navController.navigate(ReservationCompleteDestination(classId, passId))
                },
                onReservationFailure = { title, message ->
                    navController.navigate(ReservationRequestErrorDestination(title, message))
                },
                onScheduleClick = ::navigateHome,
            )
        }

        composable<WaitlistReservationDestination> { backStackEntry ->
            val destination = backStackEntry.toRoute<WaitlistReservationDestination>()
            WaitlistReservationRoute(
                classId = destination.classId,
                initialPassId = destination.passId,
                onBackClick = navController::popBackStack,
                onWaitlistComplete = { classId, passId ->
                    navController.navigate(WaitlistCompleteDestination(classId, passId))
                },
                onWaitlistFailure = { title, message ->
                    navController.navigate(ReservationRequestErrorDestination(title, message))
                },
            )
        }

        composable<ReservationCompleteDestination> { backStackEntry ->
            val destination = backStackEntry.toRoute<ReservationCompleteDestination>()
            ReservationCompleteRoute(
                classId = destination.classId,
                passId = destination.passId,
                onCloseClick = ::navigateHome,
                onScheduleClick = ::navigateHome,
                onHomeClick = ::navigateHome,
            )
        }

        composable<WaitlistCompleteDestination> { backStackEntry ->
            val destination = backStackEntry.toRoute<WaitlistCompleteDestination>()
            WaitlistCompleteRoute(
                classId = destination.classId,
                passId = destination.passId,
                onCloseClick = ::navigateHome,
                onScheduleClick = ::navigateHome,
                onHomeClick = ::navigateHome,
            )
        }

        composable<ReservationRequestErrorDestination> { backStackEntry ->
            val destination = backStackEntry.toRoute<ReservationRequestErrorDestination>()
            ReservationRequestErrorScreen(
                title = destination.title,
                message = destination.message,
                onReservationClick = ::navigateHome,
                onHomeClick = ::navigateHome,
            )
        }
    }
}
