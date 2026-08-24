package com.classitda.feature.instructor.management.lesson.create.component

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import classitda.shared.generated.resources.Res
import classitda.shared.generated.resources.ic_calendar
import com.classitda.core.designsystem.AppShape
import com.classitda.core.designsystem.AppSpacing
import com.classitda.core.designsystem.AppTheme
import com.classitda.core.designsystem.InsColors
import com.classitda.core.designsystem.ThemeType
import org.jetbrains.compose.resources.painterResource

@Composable
internal fun DatePickerField(
    dateText: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .height(48.dp)
                .clip(AppShape.Card)
                .border(1.dp, InsColors.Divider, AppShape.Card)
                .clickable(onClick = onClick)
                .padding(horizontal = AppSpacing.md),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = dateText,
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.bodySmall,
            color = InsColors.TextPrimary,
        )
        Icon(
            painter = painterResource(Res.drawable.ic_calendar),
            contentDescription = null,
            modifier = Modifier.size(18.dp),
            tint = InsColors.TextSecondary,
        )
    }
}

@Composable
@Preview
private fun DatePickerFieldPreview() {
    AppTheme(theme = ThemeType.INSTRUCTOR) {
        DatePickerField(
            dateText = "2026.08.08 (토)",
            onClick = {},
        )
    }
}
