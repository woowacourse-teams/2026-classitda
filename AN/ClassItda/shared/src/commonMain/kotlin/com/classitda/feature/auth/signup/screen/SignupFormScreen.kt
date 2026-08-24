package com.classitda.feature.auth.signup.screen

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import classitda.shared.generated.resources.Res
import classitda.shared.generated.resources.classitda_wordmark
import com.classitda.core.designsystem.AppSpacing
import com.classitda.core.designsystem.AppTheme
import com.classitda.core.designsystem.StuColors
import com.classitda.core.designsystem.ThemeType
import com.classitda.core.designsystem.appTypography
import com.classitda.feature.auth.signup.SignupAction
import com.classitda.feature.auth.signup.SignupPage
import com.classitda.feature.auth.signup.SignupUiState
import com.classitda.feature.auth.signup.component.SignupActionButton
import com.classitda.feature.auth.signup.component.SignupPageScaffold
import com.classitda.feature.auth.signup.component.SignupTextField
import com.classitda.feature.auth.signup.component.SignupTextFieldWithAction
import org.jetbrains.compose.resources.painterResource

@Composable
internal fun SignupFormScreen(
    state: SignupUiState,
    onAction: (SignupAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    SignupPageScaffold(
        title = "회원가입",
        onBack = { onAction(SignupAction.Back) },
    ) {
        Column(
            modifier =
                modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = AppSpacing.xl),
        ) {
            Spacer(modifier = Modifier.height(AppSpacing.xl))
            Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.xs)) {
                Text(
                    text = "안녕하세요!",
                    style = appTypography().headlineSmall.copy(fontWeight = FontWeight.Bold),
                    color = StuColors.TextPrimary,
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Image(
                        painter = painterResource(Res.drawable.classitda_wordmark),
                        contentDescription = "클래스잇다",
                        modifier = Modifier.width(96.dp).height(30.dp),
                        contentScale = ContentScale.Fit,
                    )
                    Text(
                        text = " 입니다.",
                        style = appTypography().headlineSmall.copy(fontWeight = FontWeight.Bold),
                        color = StuColors.TextPrimary,
                    )
                }
            }
            Spacer(modifier = Modifier.height(AppSpacing.sm))
            Text(
                text = "서비스 이용을 위해 회원 정보를 입력해 주세요.",
                style = appTypography().bodySmall,
                color = StuColors.TextTertiary,
            )
            Spacer(modifier = Modifier.height(AppSpacing.xxl))
            SignupTextField(
                label = "이름",
                value = state.name,
                placeholder = "성함을 입력해 주세요",
                onValueChange = { onAction(SignupAction.ChangeName(it)) },
                keyboardType = KeyboardType.Text,
            )
            Spacer(modifier = Modifier.height(AppSpacing.lg))
            SignupTextFieldWithAction(
                label = "휴대전화 번호",
                value = state.phoneNumber,
                placeholder = "01012345678",
                actionText =
                    if (state.isPhoneVerified) {
                        "인증완료"
                    } else if (state.resendRemainingSeconds >
                        0
                    ) {
                        "대기"
                    } else if (state.isVerificationSent) {
                        "재요청"
                    } else {
                        "인증요청"
                    },
                onValueChange = { onAction(SignupAction.ChangePhoneNumber(it)) },
                onAction = { onAction(SignupAction.SendVerificationCode) },
                keyboardType = KeyboardType.Phone,
                enabled =
                    !state.isLoading &&
                        !state.isPhoneVerified &&
                        state.resendRemainingSeconds == 0L,
            )
            Spacer(modifier = Modifier.height(AppSpacing.lg))
            SignupTextField(
                label = "인증번호",
                value = state.verificationCode,
                placeholder = "인증번호 입력",
                trailingText =
                    state.verificationRemainingSeconds.takeIf { state.isVerificationSent }?.let(
                        ::formatRemainingTime,
                    ),
                onValueChange = { onAction(SignupAction.ChangeVerificationCode(it)) },
                keyboardType = KeyboardType.Number,
            )
            Spacer(modifier = Modifier.height(AppSpacing.xl))
        }
        SignupActionButton(
            text = "확인",
            color = StuColors.Gray900,
            onClick = { onAction(SignupAction.ConfirmForm) },
            modifier = Modifier.padding(horizontal = AppSpacing.xl, vertical = AppSpacing.md),
        )
    }
}

private fun formatRemainingTime(totalSeconds: Long): String {
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return "${minutes.toString().padStart(2, '0')}:${seconds.toString().padStart(2, '0')}"
}

@Preview(name = "Signup form", showBackground = true, widthDp = 390, heightDp = 844)
@Composable
private fun SignupFormScreenPreview() {
    AppTheme(theme = ThemeType.STUDENT) {
        var formState by remember {
            mutableStateOf(SignupUiState(page = SignupPage.Form))
        }

        SignupFormScreen(
            state = formState,
            onAction = { action ->
                formState =
                    when (action) {
                        is SignupAction.ChangeName -> {
                            formState.copy(name = action.value)
                        }

                        is SignupAction.ChangePhoneNumber -> {
                            formState.copy(phoneNumber = action.value)
                        }

                        is SignupAction.ChangeVerificationCode -> {
                            formState.copy(verificationCode = action.value)
                        }

                        SignupAction.SendVerificationCode -> {
                            formState.copy(isVerificationSent = true)
                        }

                        else -> {
                            formState
                        }
                    }
            },
        )
    }
}
