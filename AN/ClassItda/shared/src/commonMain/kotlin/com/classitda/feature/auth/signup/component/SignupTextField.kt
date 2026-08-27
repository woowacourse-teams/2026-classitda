package com.classitda.feature.auth.signup.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.classitda.core.designsystem.AppShape
import com.classitda.core.designsystem.AppSpacing
import com.classitda.core.designsystem.AppTheme
import com.classitda.core.designsystem.StuColors
import com.classitda.core.designsystem.ThemeType
import com.classitda.core.designsystem.appTypography

@Composable
internal fun SignupTextField(
    label: String,
    value: String,
    placeholder: String,
    onValueChange: (String) -> Unit,
    keyboardType: KeyboardType,
    trailingText: String? = null,
    errorText: String? = null,
) {
    Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.xs)) {
        Text(
            text = label,
            style = appTypography().labelSmall,
            color = StuColors.TextSecondary,
        )
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth().height(48.dp),
            placeholder = {
                Text(
                    text = placeholder,
                    style = appTypography().bodySmall,
                    color = StuColors.TextTertiary,
                )
            },
            trailingIcon =
                trailingText?.let {
                    {
                        Text(
                            text = it,
                            modifier = Modifier.padding(end = AppSpacing.md),
                            style = appTypography().labelSmall,
                            color = StuColors.TextSecondary,
                        )
                    }
                },
            singleLine = true,
            shape = AppShape.Card,
            textStyle = appTypography().bodySmall,
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType, imeAction = ImeAction.Next),
            colors = signupTextFieldColors(),
        )
        errorText?.let {
            Text(
                text = it,
                style = appTypography().labelSmall,
                color = StuColors.Red,
            )
        }
    }
}

@Composable
internal fun SignupTextFieldWithAction(
    label: String,
    value: String,
    placeholder: String,
    actionText: String,
    onValueChange: (String) -> Unit,
    onAction: () -> Unit,
    keyboardType: KeyboardType,
    enabled: Boolean = true,
    errorText: String? = null,
) {
    Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.xs)) {
        Text(
            text = label,
            style = appTypography().labelSmall,
            color = StuColors.TextSecondary,
        )
        Row(horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm)) {
            OutlinedTextField(
                value = value,
                onValueChange = onValueChange,
                modifier = Modifier.weight(1f).height(48.dp),
                placeholder = {
                    Text(
                        text = placeholder,
                        style = appTypography().bodySmall,
                        color = StuColors.TextTertiary,
                    )
                },
                singleLine = true,
                shape = AppShape.Card,
                textStyle = appTypography().bodySmall,
                keyboardOptions = KeyboardOptions(keyboardType = keyboardType, imeAction = ImeAction.Next),
                colors = signupTextFieldColors(),
            )
            Button(
                onClick = onAction,
                enabled = enabled,
                modifier = Modifier.width(68.dp).height(48.dp),
                shape = AppShape.Card,
                colors = ButtonDefaults.buttonColors(containerColor = StuColors.Gray900),
                contentPadding = PaddingValues(horizontal = AppSpacing.xs),
            ) {
                Text(
                    text = actionText,
                    style = appTypography().labelSmall.copy(fontWeight = FontWeight.SemiBold),
                    color = StuColors.White,
                )
            }
        }
        errorText?.let {
            Text(
                text = it,
                style = appTypography().labelSmall,
                color = StuColors.Red,
            )
        }
    }
}

@Composable
private fun signupTextFieldColors() =
    OutlinedTextFieldDefaults.colors(
        focusedContainerColor = StuColors.Surface,
        unfocusedContainerColor = StuColors.Surface,
        focusedBorderColor = StuColors.Primary,
        unfocusedBorderColor = StuColors.Divider,
        cursorColor = StuColors.Green,
    )

@Preview(name = "Signup text field", showBackground = true, widthDp = 360)
@Composable
private fun SignupTextFieldPreview() {
    AppTheme(theme = ThemeType.STUDENT) {
        var name by remember { mutableStateOf("") }

        SignupTextField(
            label = "이름",
            value = name,
            placeholder = "성함을 입력해 주세요",
            onValueChange = { name = it },
            keyboardType = KeyboardType.Text,
            trailingText = null,
        )
    }
}

@Preview(name = "Signup text field with action", showBackground = true, widthDp = 360)
@Composable
private fun SignupTextFieldWithActionPreview() {
    AppTheme(theme = ThemeType.STUDENT) {
        var phoneNumber by remember { mutableStateOf("") }
        var isVerificationSent by remember { mutableStateOf(false) }

        SignupTextFieldWithAction(
            label = "휴대전화 번호",
            value = phoneNumber,
            placeholder = "01012345678",
            actionText = if (isVerificationSent) "재요청" else "인증요청",
            onValueChange = { phoneNumber = it },
            onAction = { isVerificationSent = true },
            keyboardType = KeyboardType.Phone,
        )
    }
}
