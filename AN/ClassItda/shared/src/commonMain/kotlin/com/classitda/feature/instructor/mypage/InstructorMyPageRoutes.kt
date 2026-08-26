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
import co.touchlab.kermit.Logger
import com.classitda.core.platform.KakaoPostcodeResult
import com.classitda.core.platform.KakaoPostcodeSearchState
import com.classitda.core.platform.StudioImagePickerError
import com.classitda.core.platform.StudioImagePickerSelection
import com.classitda.core.platform.releaseStudioImage
import com.classitda.domain.model.instructor.mypage.StudioAddress
import com.classitda.domain.model.instructor.mypage.StudioImageSelection
import com.classitda.feature.common.profile.PhoneNumberChangeScreen
import com.classitda.feature.common.profile.ProfileEditScreen
import com.classitda.feature.common.profile.ProfileViewScreen
import com.classitda.feature.common.profile.contract.PhoneNumberChangeAction
import com.classitda.feature.common.profile.contract.ProfileEditAction
import com.classitda.feature.common.profile.contract.ProfileViewAction
import com.classitda.feature.instructor.mypage.contract.InstructorMyPageAction
import com.classitda.feature.instructor.mypage.contract.MemberEditAction
import com.classitda.feature.instructor.mypage.contract.MemberManagementAction
import com.classitda.feature.instructor.mypage.contract.MemberRegistrationAction
import com.classitda.feature.instructor.mypage.contract.StudioDetailAction
import com.classitda.feature.instructor.mypage.contract.StudioDetailUiState
import com.classitda.feature.instructor.mypage.contract.StudioEditAction
import com.classitda.feature.instructor.mypage.contract.StudioEditUiState
import com.classitda.feature.instructor.mypage.contract.StudioManagementAction
import com.classitda.feature.instructor.mypage.contract.StudioRegistrationAction
import com.classitda.feature.instructor.mypage.contract.StudioRegistrationUiState
import com.classitda.feature.instructor.mypage.member.MemberEditScreen
import com.classitda.feature.instructor.mypage.member.MemberEditViewModel
import com.classitda.feature.instructor.mypage.member.MemberManagementScreen
import com.classitda.feature.instructor.mypage.member.MemberManagementViewModel
import com.classitda.feature.instructor.mypage.member.MemberRegistrationScreen
import com.classitda.feature.instructor.mypage.member.MemberRegistrationViewModel
import com.classitda.feature.instructor.mypage.profile.InstructorPhoneNumberChangeViewModel
import com.classitda.feature.instructor.mypage.profile.InstructorProfileEditViewModel
import com.classitda.feature.instructor.mypage.profile.InstructorProfileViewModel
import com.classitda.feature.instructor.mypage.studio.StudioDetailScreen
import com.classitda.feature.instructor.mypage.studio.StudioDetailViewModel
import com.classitda.feature.instructor.mypage.studio.StudioEditScreen
import com.classitda.feature.instructor.mypage.studio.StudioEditViewModel
import com.classitda.feature.instructor.mypage.studio.StudioManagementScreen
import com.classitda.feature.instructor.mypage.studio.StudioManagementViewModel
import com.classitda.feature.instructor.mypage.studio.StudioRegistrationScreen
import com.classitda.feature.instructor.mypage.studio.StudioRegistrationViewModel
import com.classitda.feature.instructor.mypage.studio.address.KakaoPostcodeSearchDialog
import com.classitda.feature.instructor.mypage.studio.image.StudioImagePickerOverlay
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

@Composable
internal fun InstructorMyPageRoute(
    onBack: () -> Unit,
    onOpenProfile: () -> Unit,
    onOpenMemberManagement: () -> Unit,
    onOpenStudioManagement: () -> Unit,
    onOpenPrivacyPolicy: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: InstructorMyPageViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    InstructorMyPageScreen(uiState, onAction = { action ->
        when (action) {
            InstructorMyPageAction.OpenProfile -> onOpenProfile()
            InstructorMyPageAction.OpenMemberManagement -> onOpenMemberManagement()
            InstructorMyPageAction.OpenStudioManagement -> onOpenStudioManagement()
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
internal fun InstructorStudioManagementRoute(
    onBack: () -> Unit,
    onEditStudio: (com.classitda.domain.model.instructor.mypage.InstructorStudioId) -> Unit,
    onOpenStudioDetail: (com.classitda.domain.model.instructor.mypage.InstructorStudioId) -> Unit,
    onOpenStudioRegistration: () -> Unit,
    refreshToken: Int = 0,
    modifier: Modifier = Modifier,
    viewModel: StudioManagementViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    LaunchedEffect(refreshToken) { if (refreshToken > 0) viewModel.refresh() }
    StudioManagementScreen(uiState, onAction = { action ->
        when (action) {
            StudioManagementAction.Back -> onBack()
            is StudioManagementAction.EditStudio -> onEditStudio(action.studioId)
            is StudioManagementAction.OpenStudioDetail -> onOpenStudioDetail(action.studioId)
            StudioManagementAction.OpenStudioRegistration -> onOpenStudioRegistration()
            StudioManagementAction.Retry -> viewModel.onAction(action)
        }
    }, modifier = modifier)
}

@Composable
internal fun InstructorStudioDetailRoute(
    studioId: com.classitda.domain.model.instructor.mypage.InstructorStudioId,
    onBack: () -> Unit,
    onOpenEdit: (com.classitda.domain.model.instructor.mypage.InstructorStudioId) -> Unit,
    onDeleted: (com.classitda.domain.model.instructor.mypage.InstructorStudioId) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: StudioDetailViewModel =
        koinViewModel(
            key = "instructor-studio-detail-${studioId.value}",
            parameters = { parametersOf(studioId) },
        ),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    StudioDetailScreen(uiState, onAction = { action ->
        when (action) {
            StudioDetailAction.Back -> onBack()
            StudioDetailAction.OpenEdit -> onOpenEdit(studioId)
            is StudioDetailAction.DeleteAcknowledged -> onDeleted(action.studioId)
            else -> viewModel.onAction(action)
        }
    }, modifier = modifier)
}

@Composable
internal fun InstructorStudioEditRoute(
    studioId: com.classitda.domain.model.instructor.mypage.InstructorStudioId,
    onBack: () -> Unit,
    onSaved: (com.classitda.domain.model.instructor.mypage.InstructorStudioId) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: StudioEditViewModel =
        koinViewModel(
            key = "instructor-studio-edit-${studioId.value}",
            parameters = { parametersOf(studioId) },
        ),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var lastLocalStudioImageHandle by remember { mutableStateOf<String?>(null) }
    val currentLocalStudioImageHandle by rememberUpdatedState(lastLocalStudioImageHandle)
    var postcodeSearchState by remember { mutableStateOf<KakaoPostcodeSearchState?>(null) }
    var postcodeSearchSession by remember { mutableStateOf(0) }
    var imagePickerVisible by remember { mutableStateOf(false) }
    DisposableEffect(Unit) {
        onDispose {
            currentLocalStudioImageHandle?.let(::releaseStudioImage)
        }
    }
    StudioEditScreen(uiState, onAction = { action ->
        when (action) {
            StudioEditAction.Back -> {
                onBack()
            }

            StudioEditAction.RequestAddressSearch -> {
                Logger.d("StudioAddress: edit request address search")
                postcodeSearchSession += 1
                postcodeSearchState = KakaoPostcodeSearchState.Loading
            }

            StudioEditAction.RequestImageSource -> {
                imagePickerVisible = true
            }

            StudioEditAction.RemoveImage -> {
                lastLocalStudioImageHandle?.let(::releaseStudioImage)
                lastLocalStudioImageHandle = null
                viewModel.onAction(action)
            }

            is StudioEditAction.ImageSelected -> {
                lastLocalStudioImageHandle?.let { handle ->
                    if (handle != action.image.selection.localHandle()) releaseStudioImage(handle)
                }
                lastLocalStudioImageHandle = action.image.selection.localHandle()
                viewModel.onAction(action)
            }

            is StudioEditAction.SuccessAcknowledged -> {
                lastLocalStudioImageHandle?.let(::releaseStudioImage)
                lastLocalStudioImageHandle = null
                onSaved(action.studioId)
            }

            else -> {
                viewModel.onAction(action)
            }
        }
    }, modifier = modifier)
    LaunchedEffect(postcodeSearchState, postcodeSearchSession) {
        Logger.d("StudioAddress: edit postcode state=$postcodeSearchState session=$postcodeSearchSession")
    }
    StudioImagePickerOverlay(
        visible = imagePickerVisible,
        onSelected = { selection ->
            imagePickerVisible = false
            lastLocalStudioImageHandle?.let { handle ->
                if (handle != selection.handle) releaseStudioImage(handle)
            }
            lastLocalStudioImageHandle = selection.handle
            viewModel.onAction(StudioEditAction.ImageSelected(selection.toInputUiModel()))
        },
        onCancelled = { imagePickerVisible = false },
        onError = { reason ->
            imagePickerVisible = false
            viewModel.onAction(StudioEditAction.ImagePickerFailed(reason.toUiError()))
        },
    )
    postcodeSearchState?.let { state ->
        key(postcodeSearchSession) {
            KakaoPostcodeSearchDialog(
                state = state,
                onLoadingChanged = { isLoading ->
                    Logger.d("StudioAddress: edit web loading=$isLoading")
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
                    Logger.d("StudioAddress: edit address result received")
                    postcodeSearchState = null
                    viewModel.onAction(StudioEditAction.AddressSelected(result.toStudioAddress()))
                },
                onCancelled = {
                    Logger.d("StudioAddress: edit postcode cancelled")
                    postcodeSearchState = null
                },
                onError = { reason ->
                    Logger.e("StudioAddress: edit postcode error=$reason")
                    postcodeSearchState = KakaoPostcodeSearchState.Error(reason)
                },
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
internal fun InstructorStudioRegistrationRoute(
    onBack: () -> Unit,
    onSuccess: () -> Unit = {},
    modifier: Modifier = Modifier,
    viewModel: StudioRegistrationViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var lastLocalStudioImageHandle by remember { mutableStateOf<String?>(null) }
    val currentLocalStudioImageHandle by rememberUpdatedState(lastLocalStudioImageHandle)
    var postcodeSearchState by remember { mutableStateOf<KakaoPostcodeSearchState?>(null) }
    var postcodeSearchSession by remember { mutableStateOf(0) }
    var imagePickerVisible by remember { mutableStateOf(false) }
    DisposableEffect(Unit) {
        onDispose {
            currentLocalStudioImageHandle?.let(::releaseStudioImage)
        }
    }
    StudioRegistrationScreen(uiState, onAction = { action ->
        when (action) {
            StudioRegistrationAction.Back -> {
                onBack()
            }

            StudioRegistrationAction.RequestAddressSearch -> {
                Logger.d("StudioAddress: registration request address search")
                postcodeSearchSession += 1
                postcodeSearchState = KakaoPostcodeSearchState.Loading
            }

            StudioRegistrationAction.RequestImageSource -> {
                imagePickerVisible = true
            }

            StudioRegistrationAction.RemoveImage -> {
                lastLocalStudioImageHandle?.let(::releaseStudioImage)
                lastLocalStudioImageHandle = null
                viewModel.onAction(action)
            }

            is StudioRegistrationAction.ImageSelected -> {
                lastLocalStudioImageHandle?.let { handle ->
                    if (handle != action.image.selection.localHandle()) releaseStudioImage(handle)
                }
                lastLocalStudioImageHandle = action.image.selection.localHandle()
                viewModel.onAction(action)
            }

            else -> {
                viewModel.onAction(action)
            }
        }
    }, modifier = modifier)
    LaunchedEffect(postcodeSearchState, postcodeSearchSession) {
        Logger.d("StudioAddress: registration postcode state=$postcodeSearchState session=$postcodeSearchSession")
    }
    StudioImagePickerOverlay(
        visible = imagePickerVisible,
        onSelected = { selection ->
            imagePickerVisible = false
            lastLocalStudioImageHandle?.let { handle ->
                if (handle != selection.handle) releaseStudioImage(handle)
            }
            lastLocalStudioImageHandle = selection.handle
            viewModel.onAction(StudioRegistrationAction.ImageSelected(selection.toInputUiModel()))
        },
        onCancelled = { imagePickerVisible = false },
        onError = { reason ->
            imagePickerVisible = false
            viewModel.onAction(StudioRegistrationAction.ImagePickerFailed(reason.toUiError()))
        },
    )
    postcodeSearchState?.let { state ->
        key(postcodeSearchSession) {
            KakaoPostcodeSearchDialog(
                state = state,
                onLoadingChanged = { isLoading ->
                    Logger.d("StudioAddress: registration web loading=$isLoading")
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
                    Logger.d("StudioAddress: registration address result received")
                    postcodeSearchState = null
                    viewModel.onAction(StudioRegistrationAction.AddressSelected(result.toStudioAddress()))
                },
                onCancelled = {
                    Logger.d("StudioAddress: registration postcode cancelled")
                    postcodeSearchState = null
                },
                onError = { reason ->
                    Logger.e("StudioAddress: registration postcode error=$reason")
                    postcodeSearchState = KakaoPostcodeSearchState.Error(reason)
                },
                onRetry = {
                    postcodeSearchState = null
                    postcodeSearchSession += 1
                    postcodeSearchState = KakaoPostcodeSearchState.Loading
                },
            )
        }
    }
    val success = uiState as? com.classitda.feature.instructor.mypage.contract.StudioRegistrationUiState.Success
    LaunchedEffect(success) {
        if (success != null) {
            lastLocalStudioImageHandle?.let(::releaseStudioImage)
            lastLocalStudioImageHandle = null
            onSuccess()
        }
    }
}

private fun KakaoPostcodeResult.toStudioAddress(): StudioAddress =
    StudioAddress(
        zoneCode = zoneCode,
        roadAddress = roadAddress,
        jibunAddress = jibunAddress,
        buildingName = buildingName,
        detailAddress = "",
    )

private fun StudioImagePickerSelection.toInputUiModel() =
    com.classitda.feature.instructor.mypage.contract.StudioImageInputUiModel(
        StudioImageSelection.Local(
            handle = handle,
            previewReference = previewReference,
            mimeType = mimeType,
            fileName = fileName,
            sizeBytes = sizeBytes,
        ),
    )

private fun StudioImageSelection.localHandle(): String? = (this as? StudioImageSelection.Local)?.handle

private fun StudioImagePickerError.toUiError() =
    when (this) {
        StudioImagePickerError.PERMISSION_DENIED -> {
            com.classitda.feature.instructor.mypage.contract.StudioImageUiError.PERMISSION_DENIED
        }

        StudioImagePickerError.CAMERA_UNAVAILABLE -> {
            com.classitda.feature.instructor.mypage.contract.StudioImageUiError.CAMERA_UNAVAILABLE
        }

        StudioImagePickerError.READ_FAILED -> {
            com.classitda.feature.instructor.mypage.contract.StudioImageUiError.READ_FAILED
        }

        StudioImagePickerError.INVALID_MIME -> {
            com.classitda.feature.instructor.mypage.contract.StudioImageUiError.INVALID_MIME
        }

        StudioImagePickerError.FILE_TOO_LARGE -> {
            com.classitda.feature.instructor.mypage.contract.StudioImageUiError.FILE_TOO_LARGE
        }

        StudioImagePickerError.UNKNOWN -> {
            com.classitda.feature.instructor.mypage.contract.StudioImageUiError.READ_FAILED
        }
    }

private fun StudioEditUiState.localStudioImageHandle(): String? =
    (this as? StudioEditUiState.Editing)
        ?.draft
        ?.image
        ?.selection
        ?.let { it as? StudioImageSelection.Local }
        ?.handle

private fun StudioRegistrationUiState.localStudioImageHandle(): String? =
    (this as? StudioRegistrationUiState.Editing)
        ?.draft
        ?.image
        ?.selection
        ?.let { it as? StudioImageSelection.Local }
        ?.handle
