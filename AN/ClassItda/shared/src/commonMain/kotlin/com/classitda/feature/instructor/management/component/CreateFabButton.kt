package com.classitda.feature.instructor.management.component

import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import classitda.shared.generated.resources.Res
import classitda.shared.generated.resources.ic_add
import com.classitda.core.designsystem.AppTheme
import com.classitda.core.designsystem.InsColors
import com.classitda.core.designsystem.ThemeType
import org.jetbrains.compose.resources.painterResource

@Composable
internal fun CreateFabButton(
    onClick: () -> Unit,
    contentDescription: String,
    modifier: Modifier = Modifier,
) {
    FloatingActionButton(
        onClick = onClick,
        modifier = modifier,
        shape = CircleShape,
        containerColor = InsColors.Black,
        contentColor = InsColors.White,
    ) {
        Icon(
            painter = painterResource(Res.drawable.ic_add),
            contentDescription = contentDescription,
        )
    }
}

@Composable
@Preview
private fun CreateFabButtonPreview() {
    AppTheme(theme = ThemeType.INSTRUCTOR) {
        CreateFabButton(onClick = {}, contentDescription = "수업 템플릿 추가")
    }
}
