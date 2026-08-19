package com.classitda.feature.auth.signup.component

import androidx.compose.foundation.layout.Arrangement
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
import classitda.shared.generated.resources.ic_arrow_forward
import com.classitda.core.designsystem.AppShape
import com.classitda.core.designsystem.AppSpacing
import com.classitda.core.designsystem.AppTheme
import com.classitda.core.designsystem.StuColors
import com.classitda.core.designsystem.ThemeType
import com.classitda.core.designsystem.appTypography
import org.jetbrains.compose.resources.painterResource

@Composable
internal fun SignupActionButton(
    text: String,
    color: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    showArrow: Boolean = false,
) {
    Button(
        onClick = onClick,
        modifier = modifier.fillMaxWidth().height(44.dp),
        shape = AppShape.Card,
        colors = ButtonDefaults.buttonColors(containerColor = color, contentColor = StuColors.White),
        contentPadding = PaddingValues(horizontal = AppSpacing.lg),
    ) {
        Text(
            text = text,
            style = appTypography().labelMedium.copy(fontWeight = androidx.compose.ui.text.font.FontWeight.Bold),
        )
        if (showArrow) {
            Spacer(modifier = Modifier.width(AppSpacing.xs))
            Icon(
                painter = painterResource(Res.drawable.ic_arrow_forward),
                contentDescription = null,
                tint = StuColors.White,
                modifier = Modifier.size(14.dp),
            )
        }
    }
}

@Preview(name = "Signup action buttons", showBackground = true, widthDp = 320)
@Composable
private fun SignupActionButtonPreview() {
    AppTheme(theme = ThemeType.STUDENT) {
        var clickedButton by remember { mutableStateOf<String?>(null) }

        Column(
            modifier = Modifier.padding(AppSpacing.lg),
            verticalArrangement = Arrangement.spacedBy(AppSpacing.sm),
        ) {
            SignupActionButton(
                text = "확인",
                color = StuColors.Gray900,
                onClick = { clickedButton = "확인" },
            )
            SignupActionButton(
                text = "가입 완료",
                color = StuColors.Gray900,
                onClick = { clickedButton = "가입 완료" },
            )
            clickedButton?.let {
                Text(
                    text = "클릭됨: $it",
                    color = StuColors.Green,
                )
            }
        }
    }
}
