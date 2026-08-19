package com.classitda.feature.student.mypage

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.classitda.feature.student.mypage.contract.PhoneNumberChangeAction
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

@Composable
internal fun PhoneNumberChangeRoute(
    initialPhoneNumber: String,
    onBack: () -> Unit,
    onComplete: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: PhoneNumberChangeViewModel =
        koinViewModel(
            key = "phone-number-change-$initialPhoneNumber",
            parameters = { parametersOf(initialPhoneNumber) },
        ),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    PhoneNumberChangeScreen(
        uiState = uiState,
        onAction = { action ->
            when (action) {
                PhoneNumberChangeAction.Back -> onBack()

                PhoneNumberChangeAction.Complete -> onComplete()

                PhoneNumberChangeAction.Retry,
                is PhoneNumberChangeAction.PhoneNumberChanged,
                PhoneNumberChangeAction.RequestVerification,
                is PhoneNumberChangeAction.VerificationCodeChanged,
                PhoneNumberChangeAction.VerifyCode,
                -> viewModel.onAction(action)
            }
        },
        modifier = modifier,
    )
}
