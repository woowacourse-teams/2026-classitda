package com.classitda.feature.auth.signup.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import com.classitda.feature.auth.signup.SignupAction
import com.classitda.feature.auth.signup.component.ClassitdaLogo
import com.classitda.feature.auth.signup.component.SignupActionButton
import org.jetbrains.compose.resources.painterResource

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
        ClassitdaLogo(modifier = Modifier.width(150.dp).height(126.dp))
        Text(
            text = "회원가입이\n완료되었습니다",
            style = appTypography().titleLarge.copy(fontWeight = FontWeight.Bold),
            color = StuColors.TextPrimary,
            textAlign = TextAlign.Center,
        )
        Spacer(modifier = Modifier.height(AppSpacing.sm))
        Text(
            text = "이제 더욱 편리하게\n수업을 예약하고 이용해 보세요.",
            style = appTypography().bodySmall,
            color = StuColors.TextTertiary,
            textAlign = TextAlign.Center,
        )
        Spacer(modifier = Modifier.height(AppSpacing.sectionGap))
        SignupActionButton(
            text = "시작하기",
            color = StuColors.Gray900,
            onClick = { onAction(SignupAction.Close) },
        )
        Spacer(modifier = Modifier.height(AppSpacing.sm))
        TextButton(
            onClick = { onAction(SignupAction.OpenProfile) },
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(
                text = "프로필 설정하러 가기",
                style = appTypography().labelMedium,
                color = StuColors.TextSecondary,
            )
        }
        Spacer(modifier = Modifier.height(AppSpacing.xl))
    }
}

@Preview(name = "Signup completed", showBackground = true, widthDp = 390, heightDp = 844)
@Composable
private fun SignupCompletedScreenPreview() {
    AppTheme(theme = ThemeType.STUDENT) {
        var isButtonClicked by remember { mutableStateOf(false) }

        Box(modifier = Modifier.fillMaxSize()) {
            SignupCompletedScreen(onAction = { isButtonClicked = true })
            if (isButtonClicked) {
                Text(
                    text = "버튼 클릭됨",
                    modifier =
                        Modifier
                            .align(Alignment.BottomCenter)
                            .padding(bottom = AppSpacing.lg),
                    color = StuColors.Green,
                )
            }
        }
    }
}
