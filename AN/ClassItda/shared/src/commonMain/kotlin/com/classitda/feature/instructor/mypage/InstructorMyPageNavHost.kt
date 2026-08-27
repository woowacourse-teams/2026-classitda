package com.classitda.feature.instructor.mypage

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import co.touchlab.kermit.Logger
import com.classitda.domain.model.instructor.mypage.InstructorMemberId
import com.classitda.domain.model.instructor.mypage.InstructorStudioId
import com.classitda.feature.common.privacypolicy.PrivacyPolicyRoute
import kotlinx.serialization.Serializable

@Serializable
private data class InstructorMemberEditDestination(
    val memberId: String,
)

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
) {
    val navController = rememberNavController()
    var profileRefreshToken by remember { mutableStateOf(0) }
    var memberRefreshToken by remember { mutableStateOf(0) }
    var studioRefreshToken by remember { mutableStateOf(0) }
    var currentPhoneNumber by remember { mutableStateOf("") }

    NavHost(
        navController = navController,
        startDestination = InstructorMyPageDestination.F01,
        modifier = modifier,
    ) {
        composable(InstructorMyPageDestination.F01) {
            InstructorMyPageRoute(
                onBack = { navController.popBackStack() },
                onOpenProfile = { navController.navigate(InstructorMyPageDestination.F02) },
                onOpenMemberManagement = { navController.navigate(InstructorMyPageDestination.F05) },
                onOpenStudioManagement = { navController.navigate(InstructorMyPageDestination.F08) },
                onOpenPrivacyPolicy = {
                    navController.navigate(InstructorPrivacyPolicyDestination) {
                        launchSingleTop = true
                    }
                },
                bottomBar = bottomBar,
                refreshToken = profileRefreshToken,
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

        composable(InstructorMyPageDestination.F05) {
            InstructorMemberManagementRoute(
                onBack = { navController.popBackStack() },
                onEditMember = { memberId ->
                    navController.navigate(InstructorMemberEditDestination(memberId.value))
                },
                onOpenMemberRegistration = { navController.navigate(InstructorMyPageDestination.F06) },
                refreshToken = memberRefreshToken,
            )
        }

        composable(InstructorMyPageDestination.F06) {
            InstructorMemberRegistrationRoute(
                onBack = { navController.popBackStack() },
                onSuccess = {
                    memberRefreshToken++
                    navController.popBackStack(InstructorMyPageDestination.F05, false)
                },
            )
        }

        composable<InstructorMemberEditDestination> { backStackEntry ->
            val memberId = InstructorMemberId(backStackEntry.toRoute<InstructorMemberEditDestination>().memberId)
            InstructorMemberEditRoute(
                memberId = memberId,
                onBack = { navController.popBackStack() },
                onSaved = {
                    memberRefreshToken++
                    navController.popBackStack(InstructorMyPageDestination.F05, false)
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
                refreshToken = studioRefreshToken,
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
                    studioRefreshToken++
                    navController.popBackStack(InstructorMyPageDestination.F08, false)
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
                    studioRefreshToken++
                    navController.popBackStack(InstructorMyPageDestination.F08, false)
                },
            )
        }

        composable(InstructorMyPageDestination.F09) {
            InstructorStudioRegistrationRoute(
                onBack = { navController.popBackStack() },
                onSuccess = {
                    studioRefreshToken++
                    navController.popBackStack(InstructorMyPageDestination.F08, false)
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
    const val F05 = "instructor_member_management"
    const val F06 = "instructor_member_registration"
    const val F13 = "instructor_member_edit"
    const val F08 = "instructor_studio_management"
    const val F09 = "instructor_studio_registration"
    const val F10 = "instructor_studio_detail"
    const val F11 = "instructor_studio_edit"
}
