package com.classitda.feature.instructor.classsession.edit.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.classitda.core.designsystem.AppSpacing
import com.classitda.core.designsystem.AppTheme
import com.classitda.core.designsystem.InsColors
import com.classitda.core.designsystem.ThemeType

@Composable
internal fun EditCategoryChip(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier =
            modifier.selectable(
                selected = isSelected,
                role = Role.RadioButton,
                onClick = onClick,
            ),
        shape = RoundedCornerShape(8.dp),
        color = if (isSelected) InsColors.PurpleLight else InsColors.SurfaceVariant,
        contentColor = if (isSelected) InsColors.Purple else InsColors.TextSecondary,
        border = if (isSelected) BorderStroke(1.dp, InsColors.Purple) else null,
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelMedium,
            modifier = Modifier.padding(horizontal = AppSpacing.md, vertical = AppSpacing.sm),
        )
    }
}

@Preview(name = "수업 수정 카테고리 칩", showBackground = true)
@Composable
private fun EditCategoryChipPreview() {
    AppTheme(theme = ThemeType.INSTRUCTOR) {
        Row(horizontalArrangement = Arrangement.spacedBy(AppSpacing.xs)) {
            EditCategoryChip(
                text = "요가",
                isSelected = true,
                onClick = {},
            )
            EditCategoryChip(
                text = "필라테스",
                isSelected = false,
                onClick = {},
            )
        }
    }
}
