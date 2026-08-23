package com.classitda.feature.instructor.mypage

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.classitda.feature.common.profile.PhoneNumberChangeScreen
import com.classitda.feature.common.profile.ProfileEditScreen
import com.classitda.feature.common.profile.ProfileViewScreen
import com.classitda.feature.common.profile.contract.PhoneNumberChangeAction
import com.classitda.feature.common.profile.contract.ProfileEditAction
import com.classitda.feature.common.profile.contract.ProfileViewAction
import com.classitda.feature.instructor.mypage.contract.FacilityManagementAction
import com.classitda.feature.instructor.mypage.contract.FacilityRegistrationAction
import com.classitda.feature.instructor.mypage.contract.InstructorMyPageAction
import com.classitda.feature.instructor.mypage.contract.MemberManagementAction
import com.classitda.feature.instructor.mypage.contract.MemberRegistrationAction
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

@Composable
internal fun InstructorMyPageRoute(
    onBack: () -> Unit,
    onOpenProfile: () -> Unit,
    onOpenMemberManagement: () -> Unit,
    onOpenFacilityManagement: () -> Unit,
    onOpenPrivacyPolicy: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: InstructorMyPageViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    InstructorMyPageScreen(uiState, onAction = { action ->
        when (action) {
            InstructorMyPageAction.OpenProfile -> onOpenProfile()
            InstructorMyPageAction.OpenMemberManagement -> onOpenMemberManagement()
            InstructorMyPageAction.OpenFacilityManagement -> onOpenFacilityManagement()
            InstructorMyPageAction.OpenPrivacyPolicy -> onOpenPrivacyPolicy()
            InstructorMyPageAction.Retry -> viewModel.onAction(action)
        }
    }, modifier = modifier)
}

@Composable
internal fun InstructorProfileViewRoute(
    onBack: () -> Unit,
    onOpenEdit: () -> Unit,
    onRequestLogout: () -> Unit,
    onRequestWithdrawal: () -> Unit,
    refreshToken: Int = 0,
    modifier: Modifier = Modifier,
    viewModel: InstructorProfileViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    LaunchedEffect(refreshToken) { if (refreshToken > 0) viewModel.refresh() }
    ProfileViewScreen(uiState, onAction = { action ->
        when (action) {
            ProfileViewAction.Back -> onBack()
            ProfileViewAction.OpenEdit -> onOpenEdit()
            ProfileViewAction.RequestLogout -> onRequestLogout()
            ProfileViewAction.RequestWithdrawal -> onRequestWithdrawal()
            ProfileViewAction.Retry -> viewModel.onAction(action)
        }
    }, modifier = modifier)
}

@Composable
internal fun InstructorProfileEditRoute(
    onBack: () -> Unit,
    onRequestPhotoChange: () -> Unit,
    onOpenPhoneNumberChange: (String) -> Unit,
    onProfileRefreshRequested: () -> Unit = {},
    refreshToken: Int = 0,
    modifier: Modifier = Modifier,
    viewModel: InstructorProfileEditViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var wasSaving by remember { mutableStateOf(false) }
    LaunchedEffect(refreshToken) { if (refreshToken > 0) viewModel.refresh() }
    LaunchedEffect(uiState) {
        when (uiState) {
            is com.classitda.feature.common.profile.contract.ProfileEditUiState.Saving -> {
                wasSaving = true
            }

            is com.classitda.feature.common.profile.contract.ProfileEditUiState.Editing -> {
                if (wasSaving) {
                    wasSaving = false
                    onProfileRefreshRequested()
                }
            }

            else -> {
                Unit
            }
        }
    }
    ProfileEditScreen(uiState, onAction = { action ->
        when (action) {
            ProfileEditAction.Back -> {
                onBack()
            }

            ProfileEditAction.RequestPhotoChange -> {
                onRequestPhotoChange()
            }

            ProfileEditAction.OpenPhoneNumberChange -> {
                val phone =
                    (uiState as? com.classitda.feature.common.profile.contract.ProfileEditUiState.Editing)
                        ?.phoneNumber
                        .orEmpty()
                onOpenPhoneNumberChange(phone)
            }

            else -> {
                viewModel.onAction(action)
            }
        }
    }, modifier = modifier)
}

@Composable
internal fun InstructorPhoneNumberChangeRoute(
    initialPhoneNumber: String,
    onBack: () -> Unit,
    onComplete: (String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: InstructorPhoneNumberChangeViewModel =
        koinViewModel(
            key = "instructor-phone-number-change-$initialPhoneNumber",
            parameters = { parametersOf(initialPhoneNumber) },
        ),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    PhoneNumberChangeScreen(
        uiState,
        onAction = { action ->
            when (action) {
                PhoneNumberChangeAction.Back -> {
                    onBack()
                }

                PhoneNumberChangeAction.Complete -> {
                    val verified =
                        uiState as? com.classitda.feature.common.profile.contract.PhoneNumberChangeUiState.Verified
                    if (verified != null) onComplete(verified.phoneNumber)
                }

                else -> {
                    viewModel.onAction(action)
                }
            }
        },
        modifier = modifier,
    )
}

@Composable
internal fun InstructorMemberManagementRoute(
    onBack: () -> Unit,
    onOpenMember: (com.classitda.domain.model.instructor.mypage.InstructorMemberId) -> Unit,
    onOpenMemberRegistration: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: MemberManagementViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    MemberManagementScreen(uiState, onAction = { action ->
        when (action) {
            MemberManagementAction.Back -> onBack()
            is MemberManagementAction.OpenMember -> onOpenMember(action.memberId)
            MemberManagementAction.OpenMemberRegistration -> onOpenMemberRegistration()
            else -> viewModel.onAction(action)
        }
    }, modifier = modifier)
}

@Composable
internal fun InstructorMemberRegistrationRoute(
    onBack: () -> Unit,
    onOpenConfirmation: () -> Unit = {},
    modifier: Modifier = Modifier,
    viewModel: MemberRegistrationViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    MemberRegistrationScreen(uiState, onAction = { action ->
        if (action == MemberRegistrationAction.Back) {
            onBack()
        } else if (action == MemberRegistrationAction.OpenConfirmation) {
            viewModel.onAction(action)
            onOpenConfirmation()
        } else {
            viewModel.onAction(action)
        }
    }, modifier = modifier)
}

@Composable
internal fun InstructorFacilityManagementRoute(
    onBack: () -> Unit,
    onEditFacility: (com.classitda.domain.model.instructor.mypage.InstructorFacilityId) -> Unit,
    onOpenFacilityDetail: (com.classitda.domain.model.instructor.mypage.InstructorFacilityId) -> Unit,
    onOpenFacilityRegistration: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: FacilityManagementViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    FacilityManagementScreen(uiState, onAction = { action ->
        when (action) {
            FacilityManagementAction.Back -> onBack()
            is FacilityManagementAction.EditFacility -> onEditFacility(action.facilityId)
            is FacilityManagementAction.OpenFacilityDetail -> onOpenFacilityDetail(action.facilityId)
            FacilityManagementAction.OpenFacilityRegistration -> onOpenFacilityRegistration()
            FacilityManagementAction.Retry -> viewModel.onAction(action)
        }
    }, modifier = modifier)
}

@Composable
internal fun InstructorFacilityRegistrationRoute(
    onBack: () -> Unit,
    onSuccess: (com.classitda.domain.model.instructor.mypage.InstructorFacilityId) -> Unit = {},
    modifier: Modifier = Modifier,
    viewModel: FacilityRegistrationViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    FacilityRegistrationScreen(uiState, onAction = { action ->
        if (action == FacilityRegistrationAction.Back) {
            onBack()
        } else {
            viewModel.onAction(action)
        }
    }, modifier = modifier)
    val success = uiState as? com.classitda.feature.instructor.mypage.contract.FacilityRegistrationUiState.Success
    LaunchedEffect(success?.facilityId) { success?.facilityId?.let(onSuccess) }
}
