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
import com.classitda.domain.model.instructor.mypage.InstructorFacilityId
import com.classitda.domain.model.instructor.mypage.InstructorMemberId

/** Temporary feature graph used until the app-level instructor graph is assembled. */
@Composable
internal fun InstructorMyPageNavHost(modifier: Modifier = Modifier) {
    val navController = rememberNavController()
    var profileRefreshToken by remember { mutableStateOf(0) }
    var memberRefreshToken by remember { mutableStateOf(0) }
    var facilityRefreshToken by remember { mutableStateOf(0) }
    var selectedFacilityId by remember { mutableStateOf<InstructorFacilityId?>(null) }
    var selectedMemberId by remember { mutableStateOf<InstructorMemberId?>(null) }

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
                onOpenMember = { memberId ->
                    selectedMemberId = memberId
                    navController.navigate(InstructorMyPageDestination.F12)
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

        composable(InstructorMyPageDestination.F12) {
            val memberId = selectedMemberId ?: return@composable
            InstructorMemberDetailRoute(
                memberId = memberId,
                onBack = { navController.popBackStack() },
                onOpenEdit = { id ->
                    selectedMemberId = id
                    navController.navigate(InstructorMyPageDestination.F13)
                },
                onDeleted = {
                    memberRefreshToken++
                    navController.popBackStack(InstructorMyPageDestination.F05, false)
                },
            )
        }

        composable(InstructorMyPageDestination.F13) {
            val memberId = selectedMemberId ?: return@composable
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
                    selectedFacilityId = facilityId
                    navController.navigate(InstructorMyPageDestination.F11)
                },
                onOpenFacilityDetail = { facilityId ->
                    selectedFacilityId = facilityId
                    navController.navigate(InstructorMyPageDestination.F10)
                },
                onOpenFacilityRegistration = { navController.navigate(InstructorMyPageDestination.F09) },
                refreshToken = facilityRefreshToken,
            )
        }

        composable(InstructorMyPageDestination.F10) {
            val facilityId = selectedFacilityId ?: return@composable
            InstructorFacilityDetailRoute(
                facilityId = facilityId,
                onBack = { navController.popBackStack() },
                onOpenEdit = { id ->
                    selectedFacilityId = id
                    navController.navigate(InstructorMyPageDestination.F11)
                },
                onDeleted = {
                    facilityRefreshToken++
                    navController.popBackStack(InstructorMyPageDestination.F08, false)
                },
            )
        }

        composable(InstructorMyPageDestination.F11) {
            val facilityId = selectedFacilityId ?: return@composable
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
    const val F12 = "instructor_member_detail"
    const val F13 = "instructor_member_edit"
    const val F08 = "instructor_facility_management"
    const val F09 = "instructor_facility_registration"
    const val F10 = "instructor_facility_detail"
    const val F11 = "instructor_facility_edit"
}
