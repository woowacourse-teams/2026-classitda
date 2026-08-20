package com.classitda.feature.auth.signup.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import classitda.shared.generated.resources.Res
import classitda.shared.generated.resources.ic_check
import com.classitda.core.designsystem.AppTheme
import com.classitda.core.designsystem.StuColors
import com.classitda.core.designsystem.ThemeType
import com.classitda.core.designsystem.appTypography
import org.jetbrains.compose.resources.painterResource

@Composable
internal fun SignupTermRow(
    text: String,
    checked: Boolean,
    onCheckClick: () -> Unit = {},
    onViewClick: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            painter = painterResource(Res.drawable.ic_check),
            contentDescription = null,
            tint = if (checked) StuColors.TextPrimary else StuColors.DividerStrong,
            modifier = Modifier.size(16.dp).clickable(onClick = onCheckClick),
        )
        Spacer(modifier = Modifier.width(10.dp))
        Text(
            text = text,
            modifier = Modifier.weight(1f),
            style = appTypography().labelMedium,
            color = StuColors.TextSecondary,
        )
        Text(
            text = "보기",
            modifier = Modifier.clickable(onClick = onViewClick),
            style = appTypography().labelMedium.copy(textDecoration = TextDecoration.Underline),
            color = StuColors.TextTertiary,
        )
    }
}

@Preview(name = "Signup term row", showBackground = true, widthDp = 360)
@Composable
private fun SignupTermRowPreview() {
    AppTheme(theme = ThemeType.STUDENT) {
        var isLinkClicked by remember { mutableStateOf(false) }
        var isTermsAgreed by remember { mutableStateOf(false) }
        var isPrivacyPolicyAgreed by remember { mutableStateOf(true) }

        Column {
            SignupTermRow(
                text = "[필수] 이용약관 동의",
                checked = isTermsAgreed,
                onCheckClick = { isTermsAgreed = !isTermsAgreed },
                onViewClick = { isLinkClicked = true },
            )
            Spacer(modifier = Modifier.height(12.dp))
            SignupTermRow(
                text = "[필수] 개인정보 수집 및 이용 동의",
                checked = isPrivacyPolicyAgreed,
                onCheckClick = { isPrivacyPolicyAgreed = !isPrivacyPolicyAgreed },
                onViewClick = { isLinkClicked = true },
            )
            if (isLinkClicked) {
                Text(
                    text = "보기 링크 클릭됨",
                    color = StuColors.Green,
                )
            }
        }
    }
}
