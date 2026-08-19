package com.classitda.feature.auth.signup.screen

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.classitda.core.designsystem.AppSpacing
import com.classitda.core.designsystem.AppTheme
import com.classitda.core.designsystem.StuColors
import com.classitda.core.designsystem.ThemeType
import com.classitda.core.platform.isApplePlatform
import com.classitda.feature.auth.signup.SignupAction
import com.classitda.feature.auth.signup.component.ClassitdaLogo
import com.classitda.feature.auth.signup.component.ExternalLoginButton
import com.classitda.feature.auth.signup.component.ExternalLoginProvider

@Composable
internal fun SignupWelcomeScreen(
    onAction: (SignupAction) -> Unit,
    modifier: Modifier = Modifier,
    showAppleLogin: Boolean = isApplePlatform(),
) {
    Column(
        modifier = modifier.fillMaxSize().padding(horizontal = 40.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(modifier = Modifier.weight(1f))
        ClassitdaLogo(modifier = Modifier.width(360.dp).height(280.dp))
        Spacer(modifier = Modifier.height(52.dp))
        ExternalLoginButton(
            provider = ExternalLoginProvider.Google,
            onClick = { onAction(SignupAction.LoginWithGoogle) },
        )
        if (showAppleLogin) {
            Spacer(modifier = Modifier.height(AppSpacing.sm))
            ExternalLoginButton(
                provider = ExternalLoginProvider.Apple,
                onClick = { onAction(SignupAction.LoginWithApple) },
            )
        }
        Spacer(modifier = Modifier.weight(1f))
    }
}

@Preview(name = "Signup welcome", showBackground = true, widthDp = 390, heightDp = 844)
@Composable
private fun SignupWelcomeScreenPreview() {
    AppTheme(theme = ThemeType.STUDENT) {
        SignupWelcomeScreenPreviewContent(showAppleLogin = false)
    }
}

@Preview(name = "Signup welcome / iOS", showBackground = true, widthDp = 390, heightDp = 844)
@Composable
private fun SignupWelcomeScreenIosPreview() {
    AppTheme(theme = ThemeType.STUDENT) {
        SignupWelcomeScreenPreviewContent(showAppleLogin = true)
    }
}

@Composable
private fun SignupWelcomeScreenPreviewContent(showAppleLogin: Boolean) {
    var isLoginButtonClicked by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize()) {
        SignupWelcomeScreen(
            onAction = { isLoginButtonClicked = true },
            showAppleLogin = showAppleLogin,
        )
        if (isLoginButtonClicked) {
            Text(
                text = "로그인 버튼 클릭됨",
                modifier =
                    Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = AppSpacing.lg),
                color = StuColors.Green,
            )
        }
    }
}
