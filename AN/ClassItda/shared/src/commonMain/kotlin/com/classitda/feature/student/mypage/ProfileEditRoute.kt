package com.classitda.feature.student.mypage

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.classitda.feature.common.profile.ProfileEditScreen
import com.classitda.feature.common.profile.contract.ProfileEditAction
import com.classitda.feature.common.profile.contract.ProfileEditUiState
import org.koin.compose.viewmodel.koinViewModel

@Composable
internal fun ProfileEditRoute(
    onBack: () -> Unit,
    onRequestPhotoChange: () -> Unit,
    onOpenPhoneNumberChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ProfileEditViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    ProfileEditScreen(
        uiState = uiState,
        onAction = { action ->
            when (action) {
                ProfileEditAction.Back -> {
                    onBack()
                }

                ProfileEditAction.Retry,
                is ProfileEditAction.NameChanged,
                ProfileEditAction.Save,
                -> {
                    viewModel.onAction(action)
                }

                ProfileEditAction.RequestPhotoChange -> {
                    onRequestPhotoChange()
                }

                ProfileEditAction.OpenPhoneNumberChange -> {
                    onOpenPhoneNumberChange(uiState.profilePhoneNumber())
                }
            }
        },
        modifier = modifier,
    )
}

private fun ProfileEditUiState.profilePhoneNumber(): String =
    when (this) {
        is ProfileEditUiState.Editing -> phoneNumber
        is ProfileEditUiState.Saving -> phoneNumber
        is ProfileEditUiState.SaveFailed -> phoneNumber
        else -> ""
    }
