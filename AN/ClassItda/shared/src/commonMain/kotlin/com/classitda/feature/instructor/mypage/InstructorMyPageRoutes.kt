package com.classitda.feature.instructor.mypage

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.classitda.core.platform.FacilityImagePickerError
import com.classitda.core.platform.FacilityImagePickerSelection
import com.classitda.core.platform.KakaoPostcodeResult
import com.classitda.core.platform.KakaoPostcodeSearchState
import com.classitda.core.platform.releaseFacilityImage
import com.classitda.domain.model.instructor.mypage.FacilityAddress
import com.classitda.domain.model.instructor.mypage.FacilityImageSelection
import com.classitda.feature.common.profile.PhoneNumberChangeScreen
import com.classitda.feature.common.profile.ProfileEditScreen
import com.classitda.feature.common.profile.ProfileViewScreen
import com.classitda.feature.common.profile.contract.PhoneNumberChangeAction
import com.classitda.feature.common.profile.contract.ProfileEditAction
import com.classitda.feature.common.profile.contract.ProfileViewAction
import com.classitda.feature.instructor.mypage.contract.FacilityDetailAction
import com.classitda.feature.instructor.mypage.contract.FacilityDetailUiState
import com.classitda.feature.instructor.mypage.contract.FacilityEditAction
import com.classitda.feature.instructor.mypage.contract.FacilityEditUiState
import com.classitda.feature.instructor.mypage.contract.FacilityManagementAction
import com.classitda.feature.instructor.mypage.contract.FacilityRegistrationAction
import com.classitda.feature.instructor.mypage.contract.FacilityRegistrationUiState
import com.classitda.feature.instructor.mypage.contract.InstructorMyPageAction
import com.classitda.feature.instructor.mypage.contract.MemberEditAction
import com.classitda.feature.instructor.mypage.contract.MemberManagementAction
import com.classitda.feature.instructor.mypage.contract.MemberRegistrationAction
import com.classitda.feature.instructor.mypage.facility.FacilityDetailScreen
import com.classitda.feature.instructor.mypage.facility.FacilityDetailViewModel
import com.classitda.feature.instructor.mypage.facility.FacilityEditScreen
import com.classitda.feature.instructor.mypage.facility.FacilityEditViewModel
import com.classitda.feature.instructor.mypage.facility.FacilityManagementScreen
import com.classitda.feature.instructor.mypage.facility.FacilityManagementViewModel
import com.classitda.feature.instructor.mypage.facility.FacilityRegistrationScreen
import com.classitda.feature.instructor.mypage.facility.FacilityRegistrationViewModel
import com.classitda.feature.instructor.mypage.facility.address.KakaoPostcodeSearchDialog
import com.classitda.feature.instructor.mypage.facility.image.FacilityImagePickerOverlay
import com.classitda.feature.instructor.mypage.member.MemberEditScreen
import com.classitda.feature.instructor.mypage.member.MemberEditViewModel
import com.classitda.feature.instructor.mypage.member.MemberManagementScreen
import com.classitda.feature.instructor.mypage.member.MemberManagementViewModel
import com.classitda.feature.instructor.mypage.member.MemberRegistrationScreen
import com.classitda.feature.instructor.mypage.member.MemberRegistrationViewModel
import com.classitda.feature.instructor.mypage.profile.InstructorPhoneNumberChangeViewModel
import com.classitda.feature.instructor.mypage.profile.InstructorProfileEditViewModel
import com.classitda.feature.instructor.mypage.profile.InstructorProfileViewModel
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
    onEditMember: (com.classitda.domain.model.instructor.mypage.InstructorMemberId) -> Unit,
    onOpenMemberRegistration: () -> Unit,
    refreshToken: Int = 0,
    modifier: Modifier = Modifier,
    viewModel: MemberManagementViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    LaunchedEffect(refreshToken) { if (refreshToken > 0) viewModel.refresh() }
    MemberManagementScreen(uiState, onAction = { action ->
        when (action) {
            MemberManagementAction.Back -> onBack()
            is MemberManagementAction.EditMember -> onEditMember(action.memberId)
            MemberManagementAction.DeleteAcknowledged -> viewModel.refresh()
            MemberManagementAction.OpenMemberRegistration -> onOpenMemberRegistration()
            else -> viewModel.onAction(action)
        }
    }, modifier = modifier)
}

@Composable
internal fun InstructorMemberRegistrationRoute(
    onBack: () -> Unit,
    onSuccess: (com.classitda.domain.model.instructor.mypage.InstructorMemberId) -> Unit = {},
    onOpenConfirmation: () -> Unit = {},
    modifier: Modifier = Modifier,
    viewModel: MemberRegistrationViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    MemberRegistrationScreen(uiState, onAction = { action ->
        when (action) {
            MemberRegistrationAction.Back -> {
                onBack()
            }

            MemberRegistrationAction.OpenConfirmation -> {
                viewModel.onAction(action)
                onOpenConfirmation()
            }

            is MemberRegistrationAction.SuccessAcknowledged -> {
                onSuccess(action.memberId)
            }

            else -> {
                viewModel.onAction(action)
            }
        }
    }, modifier = modifier)
}

@Composable
internal fun InstructorMemberEditRoute(
    memberId: com.classitda.domain.model.instructor.mypage.InstructorMemberId,
    onBack: () -> Unit,
    onSaved: (com.classitda.domain.model.instructor.mypage.InstructorMemberId) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: MemberEditViewModel =
        koinViewModel(
            key = "instructor-member-edit-${memberId.value}",
            parameters = { parametersOf(memberId) },
        ),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    MemberEditScreen(uiState, onAction = { action ->
        when (action) {
            MemberEditAction.Back -> onBack()
            is MemberEditAction.SuccessAcknowledged -> onSaved(action.memberId)
            else -> viewModel.onAction(action)
        }
    }, modifier = modifier)
}

@Composable
internal fun InstructorFacilityManagementRoute(
    onBack: () -> Unit,
    onEditFacility: (com.classitda.domain.model.instructor.mypage.InstructorFacilityId) -> Unit,
    onOpenFacilityDetail: (com.classitda.domain.model.instructor.mypage.InstructorFacilityId) -> Unit,
    onOpenFacilityRegistration: () -> Unit,
    refreshToken: Int = 0,
    modifier: Modifier = Modifier,
    viewModel: FacilityManagementViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    LaunchedEffect(refreshToken) { if (refreshToken > 0) viewModel.refresh() }
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
internal fun InstructorFacilityDetailRoute(
    facilityId: com.classitda.domain.model.instructor.mypage.InstructorFacilityId,
    onBack: () -> Unit,
    onOpenEdit: (com.classitda.domain.model.instructor.mypage.InstructorFacilityId) -> Unit,
    onDeleted: (com.classitda.domain.model.instructor.mypage.InstructorFacilityId) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: FacilityDetailViewModel =
        koinViewModel(
            key = "instructor-facility-detail-${facilityId.value}",
            parameters = { parametersOf(facilityId) },
        ),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    FacilityDetailScreen(uiState, onAction = { action ->
        when (action) {
            FacilityDetailAction.Back -> onBack()
            FacilityDetailAction.OpenEdit -> onOpenEdit(facilityId)
            is FacilityDetailAction.DeleteAcknowledged -> onDeleted(action.facilityId)
            else -> viewModel.onAction(action)
        }
    }, modifier = modifier)
}

@Composable
internal fun InstructorFacilityEditRoute(
    facilityId: com.classitda.domain.model.instructor.mypage.InstructorFacilityId,
    onBack: () -> Unit,
    onSaved: (com.classitda.domain.model.instructor.mypage.InstructorFacilityId) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: FacilityEditViewModel =
        koinViewModel(
            key = "instructor-facility-edit-${facilityId.value}",
            parameters = { parametersOf(facilityId) },
        ),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val currentUiState by rememberUpdatedState(uiState)
    var postcodeSearchState by remember { mutableStateOf<KakaoPostcodeSearchState?>(null) }
    var postcodeSearchSession by remember { mutableStateOf(0) }
    var imagePickerVisible by remember { mutableStateOf(false) }
    DisposableEffect(Unit) {
        onDispose {
            currentUiState.localFacilityImageHandle()?.let(::releaseFacilityImage)
        }
    }
    FacilityEditScreen(uiState, onAction = { action ->
        when (action) {
            FacilityEditAction.Back -> {
                onBack()
            }

            FacilityEditAction.RequestAddressSearch -> {
                postcodeSearchSession += 1
                postcodeSearchState = KakaoPostcodeSearchState.Loading
            }

            FacilityEditAction.RequestImageSource -> {
                imagePickerVisible = true
            }

            FacilityEditAction.RemoveImage -> {
                uiState.localFacilityImageHandle()?.let(::releaseFacilityImage)
                viewModel.onAction(action)
            }

            is FacilityEditAction.ImageSelected -> {
                uiState.localFacilityImageHandle()?.let { handle ->
                    if (handle != action.image.selection.localHandle()) releaseFacilityImage(handle)
                }
                viewModel.onAction(action)
            }

            is FacilityEditAction.SuccessAcknowledged -> {
                onSaved(action.facilityId)
            }

            else -> {
                viewModel.onAction(action)
            }
        }
    }, modifier = modifier)
    FacilityImagePickerOverlay(
        visible = imagePickerVisible,
        onSelected = { selection ->
            imagePickerVisible = false
            uiState.localFacilityImageHandle()?.let(::releaseFacilityImage)
            viewModel.onAction(FacilityEditAction.ImageSelected(selection.toInputUiModel()))
        },
        onCancelled = { imagePickerVisible = false },
        onError = { reason ->
            imagePickerVisible = false
            viewModel.onAction(FacilityEditAction.ImagePickerFailed(reason.toUiError()))
        },
    )
    postcodeSearchState?.let { state ->
        key(postcodeSearchSession) {
            KakaoPostcodeSearchDialog(
                state = state,
                onLoadingChanged = { isLoading ->
                    if (postcodeSearchState !is KakaoPostcodeSearchState.Error) {
                        postcodeSearchState =
                            if (isLoading) {
                                KakaoPostcodeSearchState.Loading
                            } else {
                                KakaoPostcodeSearchState.Ready
                            }
                    }
                },
                onResult = { result ->
                    postcodeSearchState = null
                    viewModel.onAction(FacilityEditAction.AddressSelected(result.toFacilityAddress()))
                },
                onCancelled = { postcodeSearchState = null },
                onError = { reason -> postcodeSearchState = KakaoPostcodeSearchState.Error(reason) },
                onRetry = {
                    postcodeSearchState = null
                    postcodeSearchSession += 1
                    postcodeSearchState = KakaoPostcodeSearchState.Loading
                },
            )
        }
    }
}

@Composable
internal fun InstructorFacilityRegistrationRoute(
    onBack: () -> Unit,
    onSuccess: () -> Unit = {},
    modifier: Modifier = Modifier,
    viewModel: FacilityRegistrationViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val currentUiState by rememberUpdatedState(uiState)
    var postcodeSearchState by remember { mutableStateOf<KakaoPostcodeSearchState?>(null) }
    var postcodeSearchSession by remember { mutableStateOf(0) }
    var imagePickerVisible by remember { mutableStateOf(false) }
    DisposableEffect(Unit) {
        onDispose {
            currentUiState.localFacilityImageHandle()?.let(::releaseFacilityImage)
        }
    }
    FacilityRegistrationScreen(uiState, onAction = { action ->
        when (action) {
            FacilityRegistrationAction.Back -> {
                onBack()
            }

            FacilityRegistrationAction.RequestAddressSearch -> {
                postcodeSearchSession += 1
                postcodeSearchState = KakaoPostcodeSearchState.Loading
            }

            FacilityRegistrationAction.RequestImageSource -> {
                imagePickerVisible = true
            }

            FacilityRegistrationAction.RemoveImage -> {
                uiState.localFacilityImageHandle()?.let(::releaseFacilityImage)
                viewModel.onAction(action)
            }

            is FacilityRegistrationAction.ImageSelected -> {
                uiState.localFacilityImageHandle()?.let { handle ->
                    if (handle != action.image.selection.localHandle()) releaseFacilityImage(handle)
                }
                viewModel.onAction(action)
            }

            else -> {
                viewModel.onAction(action)
            }
        }
    }, modifier = modifier)
    FacilityImagePickerOverlay(
        visible = imagePickerVisible,
        onSelected = { selection ->
            imagePickerVisible = false
            uiState.localFacilityImageHandle()?.let(::releaseFacilityImage)
            viewModel.onAction(FacilityRegistrationAction.ImageSelected(selection.toInputUiModel()))
        },
        onCancelled = { imagePickerVisible = false },
        onError = { reason ->
            imagePickerVisible = false
            viewModel.onAction(FacilityRegistrationAction.ImagePickerFailed(reason.toUiError()))
        },
    )
    postcodeSearchState?.let { state ->
        key(postcodeSearchSession) {
            KakaoPostcodeSearchDialog(
                state = state,
                onLoadingChanged = { isLoading ->
                    if (postcodeSearchState !is KakaoPostcodeSearchState.Error) {
                        postcodeSearchState =
                            if (isLoading) {
                                KakaoPostcodeSearchState.Loading
                            } else {
                                KakaoPostcodeSearchState.Ready
                            }
                    }
                },
                onResult = { result ->
                    postcodeSearchState = null
                    viewModel.onAction(FacilityRegistrationAction.AddressSelected(result.toFacilityAddress()))
                },
                onCancelled = { postcodeSearchState = null },
                onError = { reason -> postcodeSearchState = KakaoPostcodeSearchState.Error(reason) },
                onRetry = {
                    postcodeSearchState = null
                    postcodeSearchSession += 1
                    postcodeSearchState = KakaoPostcodeSearchState.Loading
                },
            )
        }
    }
    val success = uiState as? com.classitda.feature.instructor.mypage.contract.FacilityRegistrationUiState.Success
    LaunchedEffect(success) { if (success != null) onSuccess() }
}

private fun KakaoPostcodeResult.toFacilityAddress(): FacilityAddress =
    FacilityAddress(
        zoneCode = zoneCode,
        roadAddress = roadAddress,
        jibunAddress = jibunAddress,
        buildingName = buildingName,
        detailAddress = "",
    )

private fun FacilityImagePickerSelection.toInputUiModel() =
    com.classitda.feature.instructor.mypage.contract.FacilityImageInputUiModel(
        FacilityImageSelection.Local(
            handle = handle,
            previewReference = previewReference,
            mimeType = mimeType,
            fileName = fileName,
            sizeBytes = sizeBytes,
        ),
    )

private fun FacilityImageSelection.localHandle(): String? = (this as? FacilityImageSelection.Local)?.handle

private fun FacilityImagePickerError.toUiError() =
    when (this) {
        FacilityImagePickerError.PERMISSION_DENIED -> {
            com.classitda.feature.instructor.mypage.contract.FacilityImageUiError.PERMISSION_DENIED
        }

        FacilityImagePickerError.CAMERA_UNAVAILABLE -> {
            com.classitda.feature.instructor.mypage.contract.FacilityImageUiError.CAMERA_UNAVAILABLE
        }

        FacilityImagePickerError.READ_FAILED -> {
            com.classitda.feature.instructor.mypage.contract.FacilityImageUiError.READ_FAILED
        }

        FacilityImagePickerError.INVALID_MIME -> {
            com.classitda.feature.instructor.mypage.contract.FacilityImageUiError.INVALID_MIME
        }

        FacilityImagePickerError.FILE_TOO_LARGE -> {
            com.classitda.feature.instructor.mypage.contract.FacilityImageUiError.FILE_TOO_LARGE
        }

        FacilityImagePickerError.UNKNOWN -> {
            com.classitda.feature.instructor.mypage.contract.FacilityImageUiError.READ_FAILED
        }
    }

private fun FacilityEditUiState.localFacilityImageHandle(): String? =
    (this as? FacilityEditUiState.Editing)
        ?.draft
        ?.image
        ?.selection
        ?.let { it as? FacilityImageSelection.Local }
        ?.handle

private fun FacilityRegistrationUiState.localFacilityImageHandle(): String? =
    (this as? FacilityRegistrationUiState.Editing)
        ?.draft
        ?.image
        ?.selection
        ?.let { it as? FacilityImageSelection.Local }
        ?.handle
