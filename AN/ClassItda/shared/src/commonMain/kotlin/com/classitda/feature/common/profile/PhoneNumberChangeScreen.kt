package com.classitda.feature.common.profile

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.foundation.text.BasicTextField
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
import androidx.compose.ui.graphics.SolidColor
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
import classitda.shared.generated.resources.phone_number_change_verify
import classitda.shared.generated.resources.phone_number_change_verifying
import com.classitda.core.designsystem.AppShape
import com.classitda.core.designsystem.AppSpacing
import com.classitda.core.designsystem.AppTheme
import com.classitda.core.designsystem.ThemeType
import com.classitda.core.designsystem.appTypography
import com.classitda.feature.common.profile.component.ProfilePrimaryButton
import com.classitda.feature.common.profile.contract.PhoneNumberChangeAction
import com.classitda.feature.common.profile.contract.PhoneNumberChangeUiError
import com.classitda.feature.common.profile.contract.PhoneNumberChangeUiState
import com.classitda.feature.common.profile.preview.ProfileBoundaryPreviewFixture
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
                ProfilePrimaryButton(
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
                tint = MaterialTheme.colorScheme.onSurface,
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
    val typography = appTypography()

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
            style = typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSurface,
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
            onVerify = { onAction(PhoneNumberChangeAction.VerifyCode) },
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
                style = typography.bodyMedium,
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
                style = typography.bodyMedium,
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
    val typography = appTypography()

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(AppSpacing.sm),
    ) {
        PhoneNumberChangeLabel(text = stringResource(Res.string.phone_number_change_phone_label))
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = AppShape.Pill,
            color = MaterialTheme.colorScheme.surface,
            border =
                BorderStroke(
                    width = AppSpacing.xs / 4,
                    color = if (isError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.outlineVariant,
                ),
        ) {
            Row(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .height(AppSpacing.xxxl + AppSpacing.xxl)
                        .padding(start = AppSpacing.lg, end = AppSpacing.sm),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm),
            ) {
                BasicTextField(
                    value = value,
                    onValueChange = onValueChange,
                    modifier = Modifier.weight(1f),
                    enabled = enabled,
                    singleLine = true,
                    textStyle =
                        typography.bodyLarge.copy(
                            color =
                                if (enabled) {
                                    MaterialTheme.colorScheme.onSurface
                                } else {
                                    MaterialTheme.colorScheme.outline
                                },
                        ),
                    cursorBrush = SolidColor(MaterialTheme.colorScheme.onSurface),
                    keyboardOptions =
                        KeyboardOptions(
                            keyboardType = KeyboardType.Phone,
                            imeAction = ImeAction.Done,
                        ),
                    keyboardActions = KeyboardActions(onDone = { onDone() }),
                    decorationBox = { innerTextField ->
                        Box(contentAlignment = Alignment.CenterStart) {
                            if (value.isEmpty()) {
                                Text(
                                    text = stringResource(Res.string.phone_number_change_phone_input),
                                    style = typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.outline,
                                )
                            }
                            innerTextField()
                        }
                    },
                )
                Button(
                    onClick = onRequest,
                    modifier =
                        Modifier.size(
                            width = VERIFICATION_BUTTON_WIDTH,
                            height = VERIFICATION_BUTTON_HEIGHT,
                        ),
                    enabled = isRequestEnabled,
                    shape = AppShape.Pill,
                    colors =
                        ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.onSurface,
                            contentColor = MaterialTheme.colorScheme.surface,
                            disabledContainerColor = MaterialTheme.colorScheme.outlineVariant,
                            disabledContentColor = MaterialTheme.colorScheme.outline,
                        ),
                    contentPadding = PaddingValues(horizontal = AppSpacing.sm),
                ) {
                    Text(
                        text = requestButtonLabel,
                        style = typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    )
                }
            }
        }
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
    onVerify: () -> Unit,
    onDone: () -> Unit,
) {
    val typography = appTypography()

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(AppSpacing.sm),
    ) {
        PhoneNumberChangeLabel(text = stringResource(Res.string.phone_number_change_code_label))
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            enabled = enabled || isVerified,
            readOnly = isVerified,
            isError = isError,
            singleLine = true,
            textStyle = typography.bodyLarge,
            placeholder = {
                Text(text = stringResource(Res.string.phone_number_change_code_placeholder))
            },
            suffix = {
                when {
                    isVerified -> {
                        VerificationCompletedChip()
                    }

                    remainingSeconds != null -> {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(AppSpacing.xs),
                        ) {
                            Text(
                                text = formatRemainingTime(remainingSeconds),
                                style = typography.bodyLarge,
                                color = MaterialTheme.colorScheme.error,
                            )
                            Button(
                                onClick = onVerify,
                                enabled =
                                    enabled &&
                                        value.length == VERIFICATION_CODE_LENGTH &&
                                        remainingSeconds > 0,
                                modifier =
                                    Modifier.size(
                                        width = VERIFICATION_BUTTON_WIDTH,
                                        height = VERIFICATION_BUTTON_HEIGHT,
                                    ),
                                shape = AppShape.Pill,
                                colors =
                                    ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.onSurface,
                                        contentColor = MaterialTheme.colorScheme.surface,
                                        disabledContainerColor = MaterialTheme.colorScheme.outlineVariant,
                                        disabledContentColor = MaterialTheme.colorScheme.outline,
                                    ),
                                contentPadding = PaddingValues(horizontal = AppSpacing.sm),
                            ) {
                                Text(
                                    text = stringResource(Res.string.phone_number_change_verify),
                                    style = typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                                )
                            }
                        }
                    }
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

@Composable
private fun VerificationCompletedChip() {
    val typography = appTypography()

    Surface(
        modifier =
            Modifier.size(
                width = VERIFICATION_BUTTON_WIDTH,
                height = VERIFICATION_BUTTON_HEIGHT,
            ),
        shape = AppShape.Pill,
        color = MaterialTheme.colorScheme.onSurface,
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = stringResource(Res.string.phone_number_change_verified),
                style = typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.surface,
            )
        }
    }
}

@Composable
private fun PhoneNumberChangeLabel(text: String) {
    val typography = appTypography()

    Text(
        text = text,
        style = typography.titleMedium.copy(fontWeight = FontWeight.Bold),
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
}

@Composable
private fun phoneNumberChangeTextFieldColors() =
    OutlinedTextFieldDefaults.colors(
        focusedContainerColor = MaterialTheme.colorScheme.surface,
        unfocusedContainerColor = MaterialTheme.colorScheme.surface,
        disabledContainerColor = MaterialTheme.colorScheme.surface,
        focusedBorderColor = MaterialTheme.colorScheme.onSurface,
        unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
        disabledBorderColor = MaterialTheme.colorScheme.outlineVariant,
        cursorColor = MaterialTheme.colorScheme.onSurface,
        errorBorderColor = MaterialTheme.colorScheme.error,
    )

@Composable
private fun PhoneNumberChangeLoadingContent(modifier: Modifier = Modifier) {
    val typography = appTypography()

    Column(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
        Spacer(modifier = Modifier.height(AppSpacing.lg))
        Text(
            text = stringResource(Res.string.phone_number_change_loading),
            style = typography.bodyMedium,
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

        PhoneNumberChangeUiError.VERIFICATION_EXPIRED -> {
            stringResource(Res.string.phone_number_change_expired)
        }

        PhoneNumberChangeUiError.VERIFICATION_FAILED -> {
            stringResource(Res.string.phone_number_change_verification_failed)
        }

        PhoneNumberChangeUiError.REQUEST_FAILED -> {
            stringResource(Res.string.phone_number_change_request_failed)
        }

        else -> {
            stringResource(Res.string.phone_number_change_unknown_error)
        }
    }

private data class PhoneNumberChangeRenderState(
    val phoneNumber: String,
    val verificationCode: String,
    val remainingSeconds: Int?,
    val errorReason: PhoneNumberChangeUiError?,
    val isPhoneInputEnabled: Boolean,
    val isCodeInputEnabled: Boolean,
    val isRequestEnabled: Boolean,
    val isRequesting: Boolean,
    val isVerifying: Boolean,
    val isVerified: Boolean,
    val shouldRetry: Boolean,
) {
    val isRequestError: Boolean
        get() = errorReason == PhoneNumberChangeUiError.REQUEST_FAILED

    val isVerificationError: Boolean
        get() =
            errorReason == PhoneNumberChangeUiError.VERIFICATION_EXPIRED ||
                errorReason == PhoneNumberChangeUiError.VERIFICATION_FAILED
}

private fun PhoneNumberChangeUiState.toRenderState(): PhoneNumberChangeRenderState =
    when (this) {
        is PhoneNumberChangeUiState.Editing -> {
            PhoneNumberChangeRenderState(
                phoneNumber = phoneNumber,
                verificationCode = verificationCode,
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
                verificationCode = verificationCode,
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
            val isExpired = reason == PhoneNumberChangeUiError.VERIFICATION_EXPIRED
            val isRequestFailure = reason == PhoneNumberChangeUiError.REQUEST_FAILED
            PhoneNumberChangeRenderState(
                phoneNumber = phoneNumber,
                verificationCode = verificationCode,
                remainingSeconds = remainingSeconds ?: if (isExpired) 0 else null,
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
        PhoneNumberChangeAction.Back -> {
            PhoneNumberChangePreviewFixture.beforeRequest
        }

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
            PhoneNumberChangeUiState.Verified(
                phoneNumber = phoneNumberForPreview(),
                verificationCode = verificationCodeForPreview(),
            )
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

private fun PhoneNumberChangeUiState.verificationCodeForPreview(): String =
    when (this) {
        is PhoneNumberChangeUiState.Editing -> verificationCode
        is PhoneNumberChangeUiState.Requesting -> verificationCode
        is PhoneNumberChangeUiState.CodeEntry -> verificationCode
        is PhoneNumberChangeUiState.Verifying -> verificationCode
        is PhoneNumberChangeUiState.Verified -> verificationCode
        is PhoneNumberChangeUiState.Error -> verificationCode
        PhoneNumberChangeUiState.Loading -> PhoneNumberChangePreviewFixture.VERIFICATION_CODE
    }

private object PhoneNumberChangePreviewFixture {
    const val PHONE_NUMBER = "01012345678"
    const val VERIFICATION_CODE = "123456"

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
            remainingSeconds = PREVIEW_REMAINING_SECONDS,
        )
    val codeEntryReady =
        PhoneNumberChangeUiState.CodeEntry(
            phoneNumber = PHONE_NUMBER,
            verificationCode = VERIFICATION_CODE,
            remainingSeconds = PREVIEW_REMAINING_SECONDS,
        )
    val verifying =
        PhoneNumberChangeUiState.Verifying(
            phoneNumber = PHONE_NUMBER,
            verificationCode = VERIFICATION_CODE,
            remainingSeconds = 125,
        )
    val verified =
        PhoneNumberChangeUiState.Verified(
            phoneNumber = PHONE_NUMBER,
            verificationCode = VERIFICATION_CODE,
        )
    val expired =
        PhoneNumberChangeUiState.Error(
            phoneNumber = PHONE_NUMBER,
            verificationCode = VERIFICATION_CODE,
            reason = PhoneNumberChangeUiError.VERIFICATION_EXPIRED,
        )
    val requestFailed =
        PhoneNumberChangeUiState.Error(
            phoneNumber = PHONE_NUMBER,
            verificationCode = "",
            reason = PhoneNumberChangeUiError.REQUEST_FAILED,
        )
    val verificationFailed =
        PhoneNumberChangeUiState.Error(
            phoneNumber = PHONE_NUMBER,
            verificationCode = VERIFICATION_CODE,
            reason = PhoneNumberChangeUiError.VERIFICATION_FAILED,
            remainingSeconds = 125,
        )
}

private const val VERIFICATION_CODE_LENGTH = 6
private const val SECONDS_PER_MINUTE = 60
private const val PREVIEW_REMAINING_SECONDS = 180
private val VERIFICATION_BUTTON_WIDTH = AppSpacing.xxxl * 2 + AppSpacing.lg
private val VERIFICATION_BUTTON_HEIGHT = AppSpacing.xxl + AppSpacing.lg

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
    name = "Code entry · Student · Confirm enabled",
    group = "Screen/PhoneNumberChange",
    widthDp = 390,
    heightDp = 840,
)
@Composable
private fun PhoneNumberChangeScreenPreview_CodeEntry_Student_ConfirmEnabled() {
    AppTheme(theme = ThemeType.STUDENT) {
        PhoneNumberChangeScreen(
            uiState = PhoneNumberChangePreviewFixture.codeEntryReady,
            onAction = {},
        )
    }
}

@Preview(
    name = "Code entry · Instructor · Confirm enabled",
    group = "Screen/PhoneNumberChange",
    widthDp = 390,
    heightDp = 840,
)
@Composable
private fun PhoneNumberChangeScreenPreview_CodeEntry_Instructor_ConfirmEnabled() {
    AppTheme(theme = ThemeType.INSTRUCTOR) {
        PhoneNumberChangeScreen(
            uiState = PhoneNumberChangePreviewFixture.codeEntryReady,
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
        val typography = appTypography()

        Column(modifier = Modifier.fillMaxSize()) {
            Text(
                text = "마지막 행동: $lastAction",
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = AppSpacing.screenPadding, vertical = AppSpacing.sm),
                style = typography.labelLarge,
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

@Preview(
    name = "F05 · Long content · Large font · Small screen",
    group = "Boundary/MyPageProfile",
    widthDp = 320,
    heightDp = 568,
    fontScale = 1.5f,
)
@Composable
private fun PhoneNumberChangeScreenPreview_Boundary_LongContent_LargeFont_SmallScreen() {
    AppTheme(theme = ThemeType.STUDENT) {
        PhoneNumberChangeScreen(
            uiState = ProfileBoundaryPreviewFixture.phoneNumberChangeState,
            onAction = {},
        )
    }
}
