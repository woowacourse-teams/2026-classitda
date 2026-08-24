package com.classitda.feature.instructor.management.`class`.component

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import classitda.shared.generated.resources.Res
import classitda.shared.generated.resources.ic_add
import classitda.shared.generated.resources.ic_check
import com.classitda.core.designsystem.AppTheme
import com.classitda.core.designsystem.InsColors
import com.classitda.core.designsystem.ThemeType
import org.jetbrains.compose.resources.painterResource

@Composable
internal fun ClassManagementAddButton(
    onCreateTemplateClick: () -> Unit,
    onCreateSessionClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var isMenuExpanded by remember { mutableStateOf(false) }

    Box(modifier = modifier) {
        FloatingActionButton(
            onClick = { isMenuExpanded = true },
            shape = CircleShape,
            containerColor = InsColors.Black,
            contentColor = InsColors.White,
        ) {
            Icon(
                painter = painterResource(Res.drawable.ic_add),
                contentDescription = "수업 추가",
            )
        }

        DropdownMenu(
            expanded = isMenuExpanded,
            onDismissRequest = { isMenuExpanded = false },
        ) {
            DropdownMenuItem(
                text = { Text(text = "템플릿 생성") },
                onClick = {
                    isMenuExpanded = false
                    onCreateTemplateClick()
                },
            )
            DropdownMenuItem(
                text = { Text(text = "수업 생성") },
                onClick = {
                    isMenuExpanded = false
                    onCreateSessionClick()
                },
            )
        }
    }
}

@Composable
@Preview
private fun ClassManagementAddButtonPreview() {
    AppTheme(theme = ThemeType.INSTRUCTOR) {
        ClassManagementAddButton(
            onCreateTemplateClick = {},
            onCreateSessionClick = {},
        )
    }
}
