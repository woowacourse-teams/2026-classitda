package com.classitda.feature.auth.signup.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import classitda.shared.generated.resources.Res
import classitda.shared.generated.resources.ic_check
import com.classitda.core.designsystem.AppTheme
import com.classitda.core.designsystem.StuColors
import com.classitda.core.designsystem.ThemeType
import org.jetbrains.compose.resources.painterResource

@Composable
internal fun SignupCheckBox(
    checked: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier =
            modifier
                .size(20.dp)
                .clip(CircleShape)
                .background(if (checked) StuColors.Gray900 else StuColors.Surface)
                .border(1.dp, if (checked) StuColors.Gray900 else StuColors.DividerStrong, CircleShape)
                .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        if (checked) {
            Icon(
                painter = painterResource(Res.drawable.ic_check),
                contentDescription = null,
                tint = StuColors.White,
                modifier = Modifier.size(14.dp),
            )
        }
    }
}

@Preview(name = "Signup checkbox", showBackground = true, widthDp = 100, heightDp = 60)
@Composable
private fun SignupCheckBoxPreview() {
    AppTheme(theme = ThemeType.STUDENT) {
        var checked by remember { mutableStateOf(false) }

        SignupCheckBox(
            checked = checked,
            onClick = { checked = !checked },
        )
    }
}
