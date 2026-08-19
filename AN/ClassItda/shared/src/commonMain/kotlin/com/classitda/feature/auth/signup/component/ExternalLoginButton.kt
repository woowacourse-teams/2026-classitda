package com.classitda.feature.auth.signup.component

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import classitda.shared.generated.resources.Res
import classitda.shared.generated.resources.ic_apple
import classitda.shared.generated.resources.ic_google
import com.classitda.core.designsystem.AppShape
import com.classitda.core.designsystem.AppSpacing
import com.classitda.core.designsystem.AppTheme
import com.classitda.core.designsystem.StuColors
import com.classitda.core.designsystem.ThemeType
import com.classitda.core.designsystem.appTypography
import org.jetbrains.compose.resources.painterResource

internal enum class ExternalLoginProvider(
    val label: String,
) {
    Google(label = "구글로 로그인"),
    Apple(label = "애플로 로그인"),
}

@Composable
internal fun ExternalLoginButton(
    provider: ExternalLoginProvider,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val isApple = provider == ExternalLoginProvider.Apple
    val iconResource =
        when (provider) {
            ExternalLoginProvider.Google -> Res.drawable.ic_google
            ExternalLoginProvider.Apple -> Res.drawable.ic_apple
        }

    Button(
        onClick = onClick,
        modifier = modifier.fillMaxWidth().height(44.dp),
        shape = AppShape.Card,
        colors =
            ButtonDefaults.buttonColors(
                containerColor = if (isApple) StuColors.Gray900 else StuColors.SurfaceVariant,
                contentColor = if (isApple) StuColors.White else StuColors.TextPrimary,
            ),
        contentPadding = PaddingValues(horizontal = AppSpacing.lg),
    ) {
        Icon(
            painter = painterResource(iconResource),
            contentDescription = null,
            tint = if (isApple) StuColors.White else Color.Unspecified,
            modifier = Modifier.size(18.dp),
        )
        Spacer(modifier = Modifier.width(AppSpacing.sm))
        Text(
            text = provider.label,
            style = appTypography().labelMedium,
        )
    }
}

@Preview(name = "External login buttons", showBackground = true, widthDp = 320)
@Composable
private fun ExternalLoginButtonPreview() {
    AppTheme(theme = ThemeType.STUDENT) {
        var selectedProvider by remember { mutableStateOf<ExternalLoginProvider?>(null) }

        Column(
            modifier = Modifier.padding(AppSpacing.lg),
        ) {
            ExternalLoginButton(
                provider = ExternalLoginProvider.Google,
                onClick = { selectedProvider = ExternalLoginProvider.Google },
            )
            Spacer(modifier = Modifier.height(AppSpacing.sm))
            ExternalLoginButton(
                provider = ExternalLoginProvider.Apple,
                onClick = { selectedProvider = ExternalLoginProvider.Apple },
            )
            selectedProvider?.let {
                Text(
                    text = "선택됨: ${it.label}",
                    modifier = Modifier.padding(top = AppSpacing.sm),
                    color = StuColors.Green,
                )
            }
        }
    }
}
