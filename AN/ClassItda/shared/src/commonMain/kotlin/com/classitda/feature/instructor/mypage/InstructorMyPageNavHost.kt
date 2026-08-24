package com.classitda.feature.instructor.mypage

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.navArgument
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.classitda.domain.model.instructor.mypage.InstructorFacilityId
import com.classitda.domain.model.instructor.mypage.InstructorMemberId

/** Temporary feature graph used until the app-level instructor graph is assembled. */
@Composable
internal fun InstructorMyPageNavHost(modifier: Modifier = Modifier) {
    val navController = rememberNavController()
    var profileRefreshToken by remember { mutableStateOf(0) }
    var memberRefreshToken by remember { mutableStateOf(0) }
    var facilityRefreshToken by remember { mutableStateOf(0) }

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
                onOpenFacilityManagement = { navController.navigate(InstructorMyPageDestination.F08) },
                onOpenPrivacyPolicy = {},
            )
        }

        composable(InstructorMyPageDestination.F02) {
            InstructorProfileViewRoute(
                onBack = { navController.popBackStack() },
                onOpenEdit = { navController.navigate(InstructorMyPageDestination.F03) },
                onRequestLogout = {},
                onRequestWithdrawal = {},
                refreshToken = profileRefreshToken,
            )
        }

        composable(InstructorMyPageDestination.F03) {
            InstructorProfileEditRoute(
                onBack = { navController.popBackStack() },
                onRequestPhotoChange = {},
                onOpenPhoneNumberChange = { navController.navigate(InstructorMyPageDestination.F04) },
                onProfileRefreshRequested = { profileRefreshToken++ },
            )
        }

        composable(InstructorMyPageDestination.F04) {
            InstructorPhoneNumberChangeRoute(
                initialPhoneNumber = "01012345678",
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
                    navController.navigate("${InstructorMyPageDestination.F13}/${memberId.value}")
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

        composable(
            route = InstructorMyPageDestination.F13WithArgument,
            arguments = listOf(navArgument(InstructorMyPageDestination.MEMBER_ID_ARG) { type = NavType.StringType }),
        ) { backStackEntry ->
            val memberId =
                backStackEntry.arguments?.getString(InstructorMyPageDestination.MEMBER_ID_ARG)
                    ?.let(::InstructorMemberId)
                    ?: return@composable
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
            InstructorFacilityManagementRoute(
                onBack = { navController.popBackStack() },
                onEditFacility = { facilityId ->
                    navController.navigate("${InstructorMyPageDestination.F11}/${facilityId.value}")
                },
                onOpenFacilityDetail = { facilityId ->
                    navController.navigate("${InstructorMyPageDestination.F10}/${facilityId.value}")
                },
                onOpenFacilityRegistration = { navController.navigate(InstructorMyPageDestination.F09) },
                refreshToken = facilityRefreshToken,
            )
        }

        composable(
            route = InstructorMyPageDestination.F10WithArgument,
            arguments = listOf(navArgument(InstructorMyPageDestination.FACILITY_ID_ARG) { type = NavType.StringType }),
        ) { backStackEntry ->
            val facilityId =
                backStackEntry.arguments?.getString(InstructorMyPageDestination.FACILITY_ID_ARG)
                    ?.let(::InstructorFacilityId)
                    ?: return@composable
            InstructorFacilityDetailRoute(
                facilityId = facilityId,
                onBack = { navController.popBackStack() },
                onOpenEdit = { id ->
                    navController.navigate("${InstructorMyPageDestination.F11}/${id.value}")
                },
                onDeleted = {
                    facilityRefreshToken++
                    navController.popBackStack(InstructorMyPageDestination.F08, false)
                },
            )
        }

        composable(
            route = InstructorMyPageDestination.F11WithArgument,
            arguments = listOf(navArgument(InstructorMyPageDestination.FACILITY_ID_ARG) { type = NavType.StringType }),
        ) { backStackEntry ->
            val facilityId =
                backStackEntry.arguments?.getString(InstructorMyPageDestination.FACILITY_ID_ARG)
                    ?.let(::InstructorFacilityId)
                    ?: return@composable
            InstructorFacilityEditRoute(
                facilityId = facilityId,
                onBack = { navController.popBackStack() },
                onSaved = {
                    facilityRefreshToken++
                    navController.popBackStack(InstructorMyPageDestination.F08, false)
                },
            )
        }

        composable(InstructorMyPageDestination.F09) {
            InstructorFacilityRegistrationRoute(
                onBack = { navController.popBackStack() },
                onSuccess = {
                    facilityRefreshToken++
                    navController.popBackStack(InstructorMyPageDestination.F08, false)
                },
            )
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
    const val MEMBER_ID_ARG = "memberId"
    const val F13WithArgument = "$F13/{$MEMBER_ID_ARG}"
    const val F08 = "instructor_facility_management"
    const val F09 = "instructor_facility_registration"
    const val F10 = "instructor_facility_detail"
    const val F11 = "instructor_facility_edit"
    const val FACILITY_ID_ARG = "facilityId"
    const val F10WithArgument = "$F10/{$FACILITY_ID_ARG}"
    const val F11WithArgument = "$F11/{$FACILITY_ID_ARG}"
}
