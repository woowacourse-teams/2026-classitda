package com.classitda.feature.auth.signup.component

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import classitda.shared.generated.resources.Res
import classitda.shared.generated.resources.ic_arrow_back
import com.classitda.core.designsystem.AppTheme
import com.classitda.core.designsystem.StuColors
import com.classitda.core.designsystem.ThemeType
import com.classitda.core.designsystem.appTypography
import org.jetbrains.compose.resources.painterResource

@Composable
internal fun SignupPageScaffold(
    title: String,
    onBack: () -> Unit,
    content: @Composable ColumnScope.() -> Unit,
) {
    val focusManager = LocalFocusManager.current

    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectTapGestures { focusManager.clearFocus() }
                },
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().height(52.dp),
            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    painter = painterResource(Res.drawable.ic_arrow_back),
                    contentDescription = "뒤로 가기",
                    tint = StuColors.TextPrimary,
                )
            }
            Text(
                text = title,
                modifier = Modifier.weight(1f),
                style = appTypography().titleMedium.copy(fontWeight = FontWeight.Bold),
                color = StuColors.TextPrimary,
                textAlign = TextAlign.Center,
            )
            Spacer(modifier = Modifier.width(48.dp))
        }
        Column(modifier = Modifier.fillMaxSize(), content = content)
    }
}

@Preview(name = "Signup page scaffold", showBackground = true, widthDp = 390, heightDp = 240)
@Composable
private fun SignupPageScaffoldPreview() {
    AppTheme(theme = ThemeType.STUDENT) {
        SignupPageScaffold(title = "회원가입", onBack = {}) {
            Text(
                text = "화면 콘텐츠",
                modifier = Modifier.weight(1f),
                color = StuColors.TextPrimary,
            )
        }
    }
}
