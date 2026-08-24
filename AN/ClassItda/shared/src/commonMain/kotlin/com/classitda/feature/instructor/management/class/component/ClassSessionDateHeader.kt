package com.classitda.feature.instructor.management.`class`.component

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import com.classitda.core.designsystem.AppTheme
import com.classitda.core.designsystem.InsColors
import com.classitda.core.designsystem.ThemeType

@Composable
internal fun ClassSessionDateHeader(
    dateText: String,
    modifier: Modifier = Modifier,
) {
    Text(
        text = dateText,
        modifier = modifier,
        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
        color = InsColors.TextPrimary,
    )
}

@Composable
@Preview
private fun ClassSessionDateHeaderPreview() {
    AppTheme(theme = ThemeType.INSTRUCTOR) {
        ClassSessionDateHeader(dateText = "8월 12일 수요일")
    }
}
