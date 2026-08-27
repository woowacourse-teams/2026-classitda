package com.classitda.feature.instructor.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.classitda.core.designsystem.AppTheme
import com.classitda.core.designsystem.AppSpacing
import com.classitda.core.designsystem.InsColors
import com.classitda.core.designsystem.ThemeType

@Composable
internal fun InstructorClassTagChip(
    text: String,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(8.dp),
        color = InsColors.SurfaceVariant,
        contentColor = InsColors.TextSecondary,
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            modifier = Modifier.padding(horizontal = AppSpacing.md, vertical = AppSpacing.xs),
        )
    }
}

@Preview(name = "강사 수업 태그 칩", showBackground = true)
@Composable
private fun InstructorClassTagChipPreview() {
    AppTheme(theme = ThemeType.INSTRUCTOR) {
        Row(horizontalArrangement = Arrangement.spacedBy(AppSpacing.xs)) {
            InstructorClassTagChip(text = "그룹 수업")
            InstructorClassTagChip(text = "필라테스")
        }
    }
}
