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

/** Temporary feature graph used until the app-level instructor graph is assembled. */
@Composable
internal fun InstructorMyPageNavHost(modifier: Modifier = Modifier) {
    val navController = rememberNavController()
    var profileRefreshToken by remember { mutableStateOf(0) }

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
                onOpenMember = {},
                onOpenMemberRegistration = { navController.navigate(InstructorMyPageDestination.F06) },
            )
        }

        composable(InstructorMyPageDestination.F06) {
            InstructorMemberRegistrationRoute(
                onBack = { navController.popBackStack() },
            )
        }

        composable(InstructorMyPageDestination.F08) {
            InstructorFacilityManagementRoute(
                onBack = { navController.popBackStack() },
                onEditFacility = {},
                onOpenFacilityDetail = {},
                onOpenFacilityRegistration = { navController.navigate(InstructorMyPageDestination.F09) },
            )
        }

        composable(InstructorMyPageDestination.F09) {
            InstructorFacilityRegistrationRoute(
                onBack = { navController.popBackStack() },
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
    const val F08 = "instructor_facility_management"
    const val F09 = "instructor_facility_registration"
}
