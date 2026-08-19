package com.classitda.feature.student.mypage

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.semantics.error
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import classitda.shared.generated.resources.Res
import classitda.shared.generated.resources.ic_check_circle
import classitda.shared.generated.resources.ic_close
import classitda.shared.generated.resources.phone_number_change_close
import classitda.shared.generated.resources.phone_number_change_code_label
import classitda.shared.generated.resources.phone_number_change_code_placeholder
import classitda.shared.generated.resources.phone_number_change_complete
import classitda.shared.generated.resources.phone_number_change_expired
import classitda.shared.generated.resources.phone_number_change_loading
import classitda.shared.generated.resources.phone_number_change_phone_input
import classitda.shared.generated.resources.phone_number_change_phone_label
import classitda.shared.generated.resources.phone_number_change_request
import classitda.shared.generated.resources.phone_number_change_request_again
import classitda.shared.generated.resources.phone_number_change_request_failed
import classitda.shared.generated.resources.phone_number_change_requesting
import classitda.shared.generated.resources.phone_number_change_title
import classitda.shared.generated.resources.phone_number_change_unknown_error
import classitda.shared.generated.resources.phone_number_change_verification_failed
import classitda.shared.generated.resources.phone_number_change_verified
import classitda.shared.generated.resources.phone_number_change_verifying
import com.classitda.core.designsystem.AppShape
import com.classitda.core.designsystem.AppSpacing
import com.classitda.core.designsystem.AppTheme
import com.classitda.core.designsystem.ThemeType
import com.classitda.core.designsystem.component.PrimaryButton
import com.classitda.domain.model.student.mypage.PhoneVerificationId
import com.classitda.domain.repository.student.mypage.MyPageFailureReason
import com.classitda.feature.student.mypage.contract.PhoneNumberChangeAction
import com.classitda.feature.student.mypage.contract.PhoneNumberChangeUiState
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

@Composable
fun PhoneNumberChangeScreen(
    uiState: PhoneNumberChangeUiState,
    onAction: (PhoneNumberChangeAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    val isVerified = uiState is PhoneNumberChangeUiState.Verified

    Surface(
        modifier = modifier.fillMaxSize(),
        shape =
            RoundedCornerShape(
                topStart = AppSpacing.xxl,
                topEnd = AppSpacing.xxl,
            ),
        color = MaterialTheme.colorScheme.background,
    ) {
        Scaffold(
            containerColor = MaterialTheme.colorScheme.background,
            topBar = {
                PhoneNumberChangeTopBar(
                    onClose = { onAction(PhoneNumberChangeAction.Back) },
                )
            },
            bottomBar = {
                PrimaryButton(
                    text = stringResource(Res.string.phone_number_change_complete),
                    onClick = { onAction(PhoneNumberChangeAction.Complete) },
                    enabled = isVerified,
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .imePadding()
                            .padding(
                                start = AppSpacing.screenPadding,
                                end = AppSpacing.screenPadding,
                                bottom = AppSpacing.xxl,
                            ),
                )
            },
        ) { innerPadding ->
            when (uiState) {
                PhoneNumberChangeUiState.Loading -> {
                    PhoneNumberChangeLoadingContent(
                        modifier = Modifier.padding(innerPadding),
                    )
                }

                else -> {
                    PhoneNumberChangeContent(
                        renderState = uiState.toRenderState(),
                        onAction = onAction,
                        modifier = Modifier.padding(innerPadding),
                    )
                }
            }
        }
    }
}

@Composable
private fun PhoneNumberChangeTopBar(onClose: () -> Unit) {
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(horizontal = AppSpacing.xs, vertical = AppSpacing.xs),
        horizontalArrangement = Arrangement.End,
    ) {
        IconButton(onClick = onClose) {
            Icon(
                painter = painterResource(Res.drawable.ic_close),
                contentDescription = stringResource(Res.string.phone_number_change_close),
                modifier = Modifier.size(AppSpacing.xxl),
                tint = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.88f),
            )
        }
    }
}

@Composable
private fun PhoneNumberChangeContent(
    renderState: PhoneNumberChangeRenderState,
    onAction: (PhoneNumberChangeAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    val keyboardController = LocalSoftwareKeyboardController.current
    val errorMessage = renderState.errorMessage()

    Column(
        modifier =
            modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .imePadding()
                .padding(horizontal = AppSpacing.screenPadding),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(modifier = Modifier.height(AppSpacing.xxl))
        Text(
            text = stringResource(Res.string.phone_number_change_title),
            modifier =
                Modifier
                    .fillMaxWidth()
                    .semantics { heading() },
            style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center,
        )
        Spacer(modifier = Modifier.height(AppSpacing.xxxl * 2))
        PhoneNumberInput(
            value = renderState.phoneNumber,
            enabled = renderState.isPhoneInputEnabled,
            isError = renderState.isRequestError,
            requestButtonLabel = renderState.requestButtonLabel(),
            isRequestEnabled = renderState.isRequestEnabled,
            onValueChange = { onAction(PhoneNumberChangeAction.PhoneNumberChanged(it)) },
            onRequest = {
                onAction(
                    if (renderState.shouldRetry) {
                        PhoneNumberChangeAction.Retry
                    } else {
                        PhoneNumberChangeAction.RequestVerification
                    },
                )
            },
            onDone = { keyboardController?.hide() },
        )
        Spacer(modifier = Modifier.height(AppSpacing.xl))
        VerificationCodeInput(
            value = renderState.verificationCode,
            enabled = renderState.isCodeInputEnabled,
            isVerified = renderState.isVerified,
            isError = renderState.isVerificationError,
            remainingSeconds = renderState.remainingSeconds,
            onValueChange = {
                onAction(
                    PhoneNumberChangeAction.VerificationCodeChanged(
                        sanitizeVerificationCode(it),
                    ),
                )
            },
            onDone = {
                keyboardController?.hide()
                if (renderState.isCodeInputEnabled && renderState.verificationCode.length == VERIFICATION_CODE_LENGTH) {
                    onAction(PhoneNumberChangeAction.VerifyCode)
                }
            },
        )
        if (renderState.isVerifying) {
            Spacer(modifier = Modifier.height(AppSpacing.md))
            Text(
                text = stringResource(Res.string.phone_number_change_verifying),
                modifier = Modifier.fillMaxWidth(),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
        }
        if (errorMessage != null) {
            Spacer(modifier = Modifier.height(AppSpacing.md))
            Text(
                text = errorMessage,
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .semantics { error(errorMessage) },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.error,
                textAlign = TextAlign.Center,
            )
        }
        Spacer(modifier = Modifier.height(AppSpacing.xxl))
    }
}

@Composable
private fun PhoneNumberInput(
    value: String,
    enabled: Boolean,
    isError: Boolean,
    requestButtonLabel: String,
    isRequestEnabled: Boolean,
    onValueChange: (String) -> Unit,
    onRequest: () -> Unit,
    onDone: () -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(AppSpacing.sm),
    ) {
        PhoneNumberChangeLabel(text = stringResource(Res.string.phone_number_change_phone_label))
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            enabled = enabled,
            isError = isError,
            singleLine = true,
            textStyle = MaterialTheme.typography.bodyLarge,
            placeholder = {
                Text(text = stringResource(Res.string.phone_number_change_phone_input))
            },
            trailingIcon = {
                Button(
                    onClick = onRequest,
                    enabled = isRequestEnabled,
                    shape = AppShape.Card,
                    colors =
                        ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.88f),
                            contentColor = MaterialTheme.colorScheme.background,
                            disabledContainerColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.28f),
                            disabledContentColor = MaterialTheme.colorScheme.background.copy(alpha = 0.72f),
                        ),
                ) {
                    Text(
                        text = requestButtonLabel,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    )
                }
            },
            keyboardOptions =
                KeyboardOptions(
                    keyboardType = KeyboardType.Phone,
                    imeAction = ImeAction.Done,
                ),
            keyboardActions = KeyboardActions(onDone = { onDone() }),
            shape = AppShape.Pill,
            colors = phoneNumberChangeTextFieldColors(),
        )
    }
}

@Composable
private fun VerificationCodeInput(
    value: String,
    enabled: Boolean,
    isVerified: Boolean,
    isError: Boolean,
    remainingSeconds: Int?,
    onValueChange: (String) -> Unit,
    onDone: () -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(AppSpacing.sm),
    ) {
        PhoneNumberChangeLabel(text = stringResource(Res.string.phone_number_change_code_label))
        if (isVerified) {
            VerifiedCodeContent()
        } else {
            OutlinedTextField(
                value = value,
                onValueChange = onValueChange,
                modifier = Modifier.fillMaxWidth(),
                enabled = enabled,
                isError = isError,
                singleLine = true,
                textStyle = MaterialTheme.typography.bodyLarge,
                placeholder = {
                    Text(text = stringResource(Res.string.phone_number_change_code_placeholder))
                },
                suffix = {
                    if (remainingSeconds != null) {
                        Text(
                            text = formatRemainingTime(remainingSeconds),
                            style = MaterialTheme.typography.bodyLarge,
                            color =
                                if (remainingSeconds == 0) {
                                    MaterialTheme.colorScheme.error
                                } else {
                                    MaterialTheme.colorScheme.onSurface
                                },
                        )
                    }
                },
                keyboardOptions =
                    KeyboardOptions(
                        keyboardType = KeyboardType.Number,
                        imeAction = ImeAction.Done,
                    ),
                keyboardActions = KeyboardActions(onDone = { onDone() }),
                shape = AppShape.Pill,
                colors = phoneNumberChangeTextFieldColors(),
            )
        }
    }
}

@Composable
private fun VerifiedCodeContent() {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = AppShape.Pill,
        color = MaterialTheme.colorScheme.primaryContainer,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = AppSpacing.lg, vertical = AppSpacing.lg),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm),
        ) {
            Icon(
                painter = painterResource(Res.drawable.ic_check_circle),
                contentDescription = null,
                modifier = Modifier.size(AppSpacing.xl),
                tint = MaterialTheme.colorScheme.primary,
            )
            Text(
                text = stringResource(Res.string.phone_number_change_verified),
                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium),
                color = MaterialTheme.colorScheme.onPrimaryContainer,
            )
        }
    }
}

@Composable
private fun PhoneNumberChangeLabel(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
    )
}

@Composable
private fun phoneNumberChangeTextFieldColors() =
    OutlinedTextFieldDefaults.colors(
        focusedContainerColor = MaterialTheme.colorScheme.surface,
        unfocusedContainerColor = MaterialTheme.colorScheme.surface,
        disabledContainerColor = MaterialTheme.colorScheme.surface,
        focusedBorderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.72f),
        unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
        disabledBorderColor = MaterialTheme.colorScheme.outlineVariant,
        cursorColor = MaterialTheme.colorScheme.onSurface,
        errorBorderColor = MaterialTheme.colorScheme.error,
    )

@Composable
private fun PhoneNumberChangeLoadingContent(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
        Spacer(modifier = Modifier.height(AppSpacing.lg))
        Text(
            text = stringResource(Res.string.phone_number_change_loading),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun PhoneNumberChangeRenderState.requestButtonLabel(): String =
    when {
        isRequesting -> stringResource(Res.string.phone_number_change_requesting)
        shouldRetry -> stringResource(Res.string.phone_number_change_request_again)
        else -> stringResource(Res.string.phone_number_change_request)
    }

@Composable
private fun PhoneNumberChangeRenderState.errorMessage(): String? =
    when (errorReason) {
        null -> {
            null
        }

        MyPageFailureReason.VERIFICATION_EXPIRED -> {
            stringResource(Res.string.phone_number_change_expired)
        }

        MyPageFailureReason.VERIFICATION_FAILED -> {
            stringResource(Res.string.phone_number_change_verification_failed)
        }

        MyPageFailureReason.NETWORK -> {
            if (verificationId == null) {
                stringResource(Res.string.phone_number_change_request_failed)
            } else {
                stringResource(Res.string.phone_number_change_verification_failed)
            }
        }

        else -> {
            stringResource(Res.string.phone_number_change_unknown_error)
        }
    }

private data class PhoneNumberChangeRenderState(
    val phoneNumber: String,
    val verificationCode: String,
    val verificationId: PhoneVerificationId?,
    val remainingSeconds: Int?,
    val errorReason: MyPageFailureReason?,
    val isPhoneInputEnabled: Boolean,
    val isCodeInputEnabled: Boolean,
    val isRequestEnabled: Boolean,
    val isRequesting: Boolean,
    val isVerifying: Boolean,
    val isVerified: Boolean,
    val shouldRetry: Boolean,
) {
    val isRequestError: Boolean
        get() = errorReason != null && verificationId == null

    val isVerificationError: Boolean
        get() = errorReason != null && verificationId != null
}

private fun PhoneNumberChangeUiState.toRenderState(): PhoneNumberChangeRenderState =
    when (this) {
        is PhoneNumberChangeUiState.Editing -> {
            PhoneNumberChangeRenderState(
                phoneNumber = phoneNumber,
                verificationCode = verificationCode,
                verificationId = null,
                remainingSeconds = null,
                errorReason = null,
                isPhoneInputEnabled = true,
                isCodeInputEnabled = false,
                isRequestEnabled = true,
                isRequesting = false,
                isVerifying = false,
                isVerified = false,
                shouldRetry = false,
            )
        }

        is PhoneNumberChangeUiState.Requesting -> {
            PhoneNumberChangeRenderState(
                phoneNumber = phoneNumber,
                verificationCode = verificationCode,
                verificationId = null,
                remainingSeconds = null,
                errorReason = null,
                isPhoneInputEnabled = false,
                isCodeInputEnabled = false,
                isRequestEnabled = false,
                isRequesting = true,
                isVerifying = false,
                isVerified = false,
                shouldRetry = false,
            )
        }

        is PhoneNumberChangeUiState.CodeEntry -> {
            PhoneNumberChangeRenderState(
                phoneNumber = phoneNumber,
                verificationCode = verificationCode,
                verificationId = verificationId,
                remainingSeconds = remainingSeconds,
                errorReason = null,
                isPhoneInputEnabled = false,
                isCodeInputEnabled = true,
                isRequestEnabled = false,
                isRequesting = false,
                isVerifying = false,
                isVerified = false,
                shouldRetry = false,
            )
        }

        is PhoneNumberChangeUiState.Verifying -> {
            PhoneNumberChangeRenderState(
                phoneNumber = phoneNumber,
                verificationCode = verificationCode,
                verificationId = verificationId,
                remainingSeconds = remainingSeconds,
                errorReason = null,
                isPhoneInputEnabled = false,
                isCodeInputEnabled = false,
                isRequestEnabled = false,
                isRequesting = false,
                isVerifying = true,
                isVerified = false,
                shouldRetry = false,
            )
        }

        is PhoneNumberChangeUiState.Verified -> {
            PhoneNumberChangeRenderState(
                phoneNumber = phoneNumber,
                verificationCode = "",
                verificationId = null,
                remainingSeconds = null,
                errorReason = null,
                isPhoneInputEnabled = false,
                isCodeInputEnabled = false,
                isRequestEnabled = false,
                isRequesting = false,
                isVerifying = false,
                isVerified = true,
                shouldRetry = false,
            )
        }

        is PhoneNumberChangeUiState.Error -> {
            val isExpired = reason == MyPageFailureReason.VERIFICATION_EXPIRED
            val isRequestFailure = verificationId == null
            PhoneNumberChangeRenderState(
                phoneNumber = phoneNumber,
                verificationCode = verificationCode,
                verificationId = verificationId,
                remainingSeconds = if (isExpired) 0 else null,
                errorReason = reason,
                isPhoneInputEnabled = isRequestFailure,
                isCodeInputEnabled = !isRequestFailure && !isExpired,
                isRequestEnabled = isRequestFailure || isExpired,
                isRequesting = false,
                isVerifying = false,
                isVerified = false,
                shouldRetry = isRequestFailure || isExpired,
            )
        }

        PhoneNumberChangeUiState.Loading -> {
            error("Loading에는 입력 렌더링 상태가 없습니다.")
        }
    }

internal fun sanitizeVerificationCode(value: String): String =
    value
        .filter { it.isDigit() }
        .take(VERIFICATION_CODE_LENGTH)

internal fun formatRemainingTime(remainingSeconds: Int): String {
    val safeSeconds = remainingSeconds.coerceAtLeast(0)
    val minutes = (safeSeconds / SECONDS_PER_MINUTE).toString().padStart(2, '0')
    val seconds = (safeSeconds % SECONDS_PER_MINUTE).toString().padStart(2, '0')
    return "$minutes:$seconds"
}

private fun PhoneNumberChangeAction.previewLabel(): String =
    when (this) {
        PhoneNumberChangeAction.Back -> "Back"
        PhoneNumberChangeAction.Retry -> "Retry"
        is PhoneNumberChangeAction.PhoneNumberChanged -> "PhoneNumberChanged(phoneNumber=$phoneNumber)"
        PhoneNumberChangeAction.RequestVerification -> "RequestVerification"
        is PhoneNumberChangeAction.VerificationCodeChanged -> "VerificationCodeChanged(code=$verificationCode)"
        PhoneNumberChangeAction.VerifyCode -> "VerifyCode"
        PhoneNumberChangeAction.Complete -> "Complete"
    }

private fun PhoneNumberChangeUiState.reduceForPreview(action: PhoneNumberChangeAction): PhoneNumberChangeUiState =
    when (action) {
        PhoneNumberChangeAction.Back,
        PhoneNumberChangeAction.Retry,
        PhoneNumberChangeAction.Complete,
        -> {
            this
        }

        is PhoneNumberChangeAction.PhoneNumberChanged -> {
            PhoneNumberChangeUiState.Editing(
                phoneNumber = action.phoneNumber,
                verificationCode = "",
            )
        }

        PhoneNumberChangeAction.RequestVerification -> {
            PhoneNumberChangeUiState.CodeEntry(
                phoneNumber = phoneNumberForPreview(),
                verificationCode = "",
                verificationId = PhoneNumberChangePreviewFixture.verificationId,
                remainingSeconds = PREVIEW_REMAINING_SECONDS,
            )
        }

        is PhoneNumberChangeAction.VerificationCodeChanged -> {
            when (this) {
                is PhoneNumberChangeUiState.CodeEntry -> copy(verificationCode = action.verificationCode)
                else -> this
            }
        }

        PhoneNumberChangeAction.VerifyCode -> {
            PhoneNumberChangeUiState.Verified(phoneNumber = phoneNumberForPreview())
        }
    }

private fun PhoneNumberChangeUiState.phoneNumberForPreview(): String =
    when (this) {
        is PhoneNumberChangeUiState.Editing -> phoneNumber
        is PhoneNumberChangeUiState.Requesting -> phoneNumber
        is PhoneNumberChangeUiState.CodeEntry -> phoneNumber
        is PhoneNumberChangeUiState.Verifying -> phoneNumber
        is PhoneNumberChangeUiState.Verified -> phoneNumber
        is PhoneNumberChangeUiState.Error -> phoneNumber
        PhoneNumberChangeUiState.Loading -> PhoneNumberChangePreviewFixture.PHONE_NUMBER
    }

private object PhoneNumberChangePreviewFixture {
    const val PHONE_NUMBER = "01012345678"
    const val VERIFICATION_CODE = "123456"
    val verificationId = PhoneVerificationId("verification-preview")

    val beforeRequest =
        PhoneNumberChangeUiState.Editing(
            phoneNumber = PHONE_NUMBER,
            verificationCode = "",
        )
    val requesting =
        PhoneNumberChangeUiState.Requesting(
            phoneNumber = PHONE_NUMBER,
            verificationCode = "",
        )
    val codeEntry =
        PhoneNumberChangeUiState.CodeEntry(
            phoneNumber = PHONE_NUMBER,
            verificationCode = "123",
            verificationId = verificationId,
            remainingSeconds = PREVIEW_REMAINING_SECONDS,
        )
    val verifying =
        PhoneNumberChangeUiState.Verifying(
            phoneNumber = PHONE_NUMBER,
            verificationCode = VERIFICATION_CODE,
            verificationId = verificationId,
            remainingSeconds = 125,
        )
    val verified = PhoneNumberChangeUiState.Verified(phoneNumber = PHONE_NUMBER)
    val expired =
        PhoneNumberChangeUiState.Error(
            phoneNumber = PHONE_NUMBER,
            verificationCode = VERIFICATION_CODE,
            verificationId = verificationId,
            reason = MyPageFailureReason.VERIFICATION_EXPIRED,
        )
    val requestFailed =
        PhoneNumberChangeUiState.Error(
            phoneNumber = PHONE_NUMBER,
            verificationCode = "",
            verificationId = null,
            reason = MyPageFailureReason.NETWORK,
        )
    val verificationFailed =
        PhoneNumberChangeUiState.Error(
            phoneNumber = PHONE_NUMBER,
            verificationCode = VERIFICATION_CODE,
            verificationId = verificationId,
            reason = MyPageFailureReason.VERIFICATION_FAILED,
        )
}

private const val VERIFICATION_CODE_LENGTH = 6
private const val SECONDS_PER_MINUTE = 60
private const val PREVIEW_REMAINING_SECONDS = 180

@Preview(
    name = "Before request · Student · Default",
    group = "Screen/PhoneNumberChange",
    widthDp = 390,
    heightDp = 840,
)
@Composable
private fun PhoneNumberChangeScreenPreview_BeforeRequest_Student_Default() {
    AppTheme(theme = ThemeType.STUDENT) {
        PhoneNumberChangeScreen(
            uiState = PhoneNumberChangePreviewFixture.beforeRequest,
            onAction = {},
        )
    }
}

@Preview(
    name = "Requesting · Student · Disabled",
    group = "Screen/PhoneNumberChange",
    widthDp = 390,
    heightDp = 840,
)
@Composable
private fun PhoneNumberChangeScreenPreview_Requesting_Student_Disabled() {
    AppTheme(theme = ThemeType.STUDENT) {
        PhoneNumberChangeScreen(
            uiState = PhoneNumberChangePreviewFixture.requesting,
            onAction = {},
        )
    }
}

@Preview(
    name = "Code entry · Student · 03:00",
    group = "Screen/PhoneNumberChange",
    widthDp = 390,
    heightDp = 840,
)
@Composable
private fun PhoneNumberChangeScreenPreview_CodeEntry_Student_FixedTimer() {
    AppTheme(theme = ThemeType.STUDENT) {
        PhoneNumberChangeScreen(
            uiState = PhoneNumberChangePreviewFixture.codeEntry,
            onAction = {},
        )
    }
}

@Preview(
    name = "Verifying · Student · Disabled",
    group = "Screen/PhoneNumberChange",
    widthDp = 390,
    heightDp = 840,
)
@Composable
private fun PhoneNumberChangeScreenPreview_Verifying_Student_Disabled() {
    AppTheme(theme = ThemeType.STUDENT) {
        PhoneNumberChangeScreen(
            uiState = PhoneNumberChangePreviewFixture.verifying,
            onAction = {},
        )
    }
}

@Preview(
    name = "Verified · Student · Complete enabled",
    group = "Screen/PhoneNumberChange",
    widthDp = 390,
    heightDp = 840,
)
@Composable
private fun PhoneNumberChangeScreenPreview_Verified_Student_CompleteEnabled() {
    AppTheme(theme = ThemeType.STUDENT) {
        PhoneNumberChangeScreen(
            uiState = PhoneNumberChangePreviewFixture.verified,
            onAction = {},
        )
    }
}

@Preview(
    name = "Expired · Student · Error",
    group = "Screen/PhoneNumberChange",
    widthDp = 390,
    heightDp = 840,
)
@Composable
private fun PhoneNumberChangeScreenPreview_Expired_Student_Error() {
    AppTheme(theme = ThemeType.STUDENT) {
        PhoneNumberChangeScreen(
            uiState = PhoneNumberChangePreviewFixture.expired,
            onAction = {},
        )
    }
}

@Preview(
    name = "Request failed · Student · Error",
    group = "Screen/PhoneNumberChange",
    widthDp = 390,
    heightDp = 840,
)
@Composable
private fun PhoneNumberChangeScreenPreview_RequestFailed_Student_Error() {
    AppTheme(theme = ThemeType.STUDENT) {
        PhoneNumberChangeScreen(
            uiState = PhoneNumberChangePreviewFixture.requestFailed,
            onAction = {},
        )
    }
}

@Preview(
    name = "Verification failed · Student · Error",
    group = "Screen/PhoneNumberChange",
    widthDp = 390,
    heightDp = 840,
)
@Composable
private fun PhoneNumberChangeScreenPreview_VerificationFailed_Student_Error() {
    AppTheme(theme = ThemeType.STUDENT) {
        PhoneNumberChangeScreen(
            uiState = PhoneNumberChangePreviewFixture.verificationFailed,
            onAction = {},
        )
    }
}

@Preview(
    name = "Actions · Student · Interactive",
    group = "Harness/PhoneNumberChange",
    widthDp = 390,
    heightDp = 840,
)
@Composable
private fun PhoneNumberChangeScreenPreview_Actions_Student_Interactive() {
    var uiState by remember { mutableStateOf<PhoneNumberChangeUiState>(PhoneNumberChangePreviewFixture.beforeRequest) }
    var lastAction by remember { mutableStateOf("None") }

    AppTheme(theme = ThemeType.STUDENT) {
        Column(modifier = Modifier.fillMaxSize()) {
            Text(
                text = "마지막 행동: $lastAction",
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = AppSpacing.screenPadding, vertical = AppSpacing.sm),
                style = MaterialTheme.typography.labelLarge,
            )
            PhoneNumberChangeScreen(
                uiState = uiState,
                onAction = { action ->
                    lastAction = action.previewLabel()
                    uiState = uiState.reduceForPreview(action)
                },
                modifier = Modifier.weight(1f),
            )
        }
    }
}
