package com.classitda.feature.instructor.mypage

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import co.touchlab.kermit.Logger
import com.classitda.core.studio.InstructorStudioContext
import com.classitda.domain.model.instructor.mypage.InstructorStudioId
import com.classitda.feature.common.privacypolicy.PrivacyPolicyRoute
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import org.koin.compose.koinInject

@Serializable
private data class InstructorStudioDetailDestination(
    val studioId: String,
)

@Serializable
private data class InstructorStudioEditDestination(
    val studioId: String,
)

@Serializable
private data object InstructorPrivacyPolicyDestination

@Composable
internal fun InstructorMyPageNavHost(
    bottomBar: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    onLogout: () -> Unit = {},
    onWithdrawalCompleted: () -> Unit = {},
    openStudioRegistrationRequest: Int = 0,
    onStudioRegistrationRequestConsumed: () -> Unit = {},
    refreshToken: Int = 0,
) {
    val navController = rememberNavController()
    val studioContext = koinInject<InstructorStudioContext>()
    val scope = rememberCoroutineScope()
    var profileRefreshToken by remember { mutableStateOf(0) }
    var studioRefreshToken by remember { mutableStateOf(0) }
    var currentPhoneNumber by remember { mutableStateOf("") }

    LaunchedEffect(openStudioRegistrationRequest) {
        if (openStudioRegistrationRequest > 0) {
            navController.navigate(InstructorMyPageDestination.F09) {
                launchSingleTop = true
            }
            onStudioRegistrationRequestConsumed()
        }
    }

    NavHost(
        navController = navController,
        startDestination = InstructorMyPageDestination.F01,
        modifier = modifier,
    ) {
        composable(InstructorMyPageDestination.F01) {
            InstructorMyPageRoute(
                onBack = { navController.popBackStack() },
                onOpenProfile = { navController.navigate(InstructorMyPageDestination.F02) },
                onOpenStudioManagement = { navController.navigate(InstructorMyPageDestination.F08) },
                onOpenPrivacyPolicy = {
                    navController.navigate(InstructorPrivacyPolicyDestination) {
                        launchSingleTop = true
                    }
                },
                bottomBar = bottomBar,
                refreshToken = profileRefreshToken,
                tabRefreshToken = refreshToken,
            )
        }

        composable(InstructorMyPageDestination.F02) {
            InstructorProfileViewRoute(
                onBack = { navController.popBackStack() },
                onOpenEdit = { navController.navigate(InstructorMyPageDestination.F03) },
                onRequestLogout = {
                    Logger.d("ProfileLogout: instructor my page nav host forwarded logout")
                    onLogout()
                },
                onWithdrawalCompleted = onWithdrawalCompleted,
                refreshToken = profileRefreshToken,
            )
        }

        composable(InstructorMyPageDestination.F03) {
            InstructorProfileEditRoute(
                onBack = { navController.popBackStack() },
                onRequestPhotoChange = {},
                onOpenPhoneNumberChange = { phoneNumber ->
                    currentPhoneNumber = phoneNumber
                    navController.navigate(InstructorMyPageDestination.F04)
                },
                onProfileRefreshRequested = { profileRefreshToken++ },
            )
        }

        composable(InstructorMyPageDestination.F04) {
            InstructorPhoneNumberChangeRoute(
                initialPhoneNumber = currentPhoneNumber,
                onBack = { navController.popBackStack() },
                onComplete = {
                    profileRefreshToken++
                    navController.popBackStack(InstructorMyPageDestination.F02, false)
                },
            )
        }

        composable(InstructorMyPageDestination.F08) {
            InstructorStudioManagementRoute(
                onBack = { navController.popBackStack() },
                onEditStudio = { studioId ->
                    navController.navigate(InstructorStudioEditDestination(studioId.value))
                },
                onOpenStudioDetail = { studioId ->
                    navController.navigate(InstructorStudioDetailDestination(studioId.value))
                },
                onOpenStudioRegistration = { navController.navigate(InstructorMyPageDestination.F09) },
                refreshToken = studioRefreshToken + refreshToken,
            )
        }

        composable<InstructorStudioDetailDestination> { backStackEntry ->
            val destination = backStackEntry.toRoute<InstructorStudioDetailDestination>()
            val studioId = InstructorStudioId(destination.studioId)
            InstructorStudioDetailRoute(
                studioId = studioId,
                onBack = { navController.popBackStack() },
                onOpenEdit = { id ->
                    navController.navigate(InstructorStudioEditDestination(id.value))
                },
                onDeleted = {
                    scope.launch {
                        studioContext.refreshStudios()
                        studioRefreshToken++
                        navController.popBackStack(InstructorMyPageDestination.F08, false)
                    }
                },
            )
        }

        composable<InstructorStudioEditDestination> { backStackEntry ->
            val destination = backStackEntry.toRoute<InstructorStudioEditDestination>()
            val studioId = InstructorStudioId(destination.studioId)
            InstructorStudioEditRoute(
                studioId = studioId,
                onBack = { navController.popBackStack() },
                onSaved = {
                    scope.launch {
                        studioContext.refreshStudios()
                        studioRefreshToken++
                        navController.popBackStack(InstructorMyPageDestination.F08, false)
                    }
                },
            )
        }

        composable(InstructorMyPageDestination.F09) {
            InstructorStudioRegistrationRoute(
                onBack = { navController.popBackStack() },
                onSuccess = {
                    scope.launch {
                        try {
                            studioContext.refreshStudios()
                        } catch (exception: CancellationException) {
                            throw exception
                        } catch (exception: Throwable) {
                            Logger.e("StudioContext: refresh after registration failed: ${exception.message}")
                        }
                        studioRefreshToken++
                        if (!navController.popBackStack(InstructorMyPageDestination.F08, false)) {
                            navController.navigate(InstructorMyPageDestination.F08) {
                                popUpTo(InstructorMyPageDestination.F09) { inclusive = true }
                                launchSingleTop = true
                            }
                        }
                    }
                },
            )
        }

        composable<InstructorPrivacyPolicyDestination> {
            PrivacyPolicyRoute(onBack = navController::popBackStack)
        }
    }
}

private object InstructorMyPageDestination {
    const val F01 = "instructor_mypage"
    const val F02 = "instructor_profile_view"
    const val F03 = "instructor_profile_edit"
    const val F04 = "instructor_phone_change"
    const val F08 = "instructor_studio_management"
    const val F09 = "instructor_studio_registration"
    const val F10 = "instructor_studio_detail"
    const val F11 = "instructor_studio_edit"
}
