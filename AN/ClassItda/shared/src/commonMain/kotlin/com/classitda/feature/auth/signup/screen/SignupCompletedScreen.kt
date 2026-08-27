package com.classitda.feature.auth.signup.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.classitda.core.designsystem.AppSpacing
import com.classitda.core.designsystem.AppTheme
import com.classitda.core.designsystem.StuColors
import com.classitda.core.designsystem.ThemeType
import com.classitda.core.designsystem.appTypography
import com.classitda.core.designsystem.component.PrimaryButton
import com.classitda.feature.auth.signup.SignupAction
import com.classitda.feature.auth.signup.component.ClassitdaCharacter
import com.classitda.feature.auth.signup.component.ClassitdaLogo
import com.classitda.feature.auth.signup.component.ClassitdaWordmark

@Composable
internal fun SignupCompletedScreen(
    onAction: (SignupAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier =
            modifier
                .fillMaxSize()
                .padding(horizontal = AppSpacing.xl),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        ClassitdaCharacter(modifier = Modifier.size(width = 130.dp, height = 80.dp).align(Alignment.CenterHorizontally))
        ClassitdaWordmark(modifier = Modifier.size(width = 130.dp, height = 50.dp))
        Text(
            text = "회원가입이\n완료되었습니다",
            style = appTypography().headlineMedium.copy(fontWeight = FontWeight.Bold),
            color = StuColors.TextPrimary,
            textAlign = TextAlign.Center,
        )
        Spacer(modifier = Modifier.height(AppSpacing.md))
        Text(
            text = "이제 언제 어디서나 편리하게\n수업을 관리하고 이용해 보세요.",
            style = appTypography().bodyMedium,
            color = StuColors.TextTertiary,
            textAlign = TextAlign.Center,
        )
        Spacer(modifier = Modifier.height(70.dp))
        PrimaryButton(
            text = "시작하기",
            onClick = { onAction(SignupAction.Close) },
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Preview(name = "Signup completed", showBackground = true, widthDp = 390, heightDp = 844)
@Composable
private fun SignupCompletedScreenPreview() {
    AppTheme(theme = ThemeType.STUDENT) {
        SignupCompletedScreen(onAction = {}, modifier = Modifier.fillMaxSize())
    }
}
