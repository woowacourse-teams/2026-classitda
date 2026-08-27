package com.classitda.feature.instructor.management.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.classitda.core.designsystem.AppShape
import com.classitda.core.designsystem.AppSpacing
import com.classitda.core.designsystem.AppTheme
import com.classitda.core.designsystem.InsColors
import com.classitda.core.designsystem.ThemeType

@Composable
internal fun OutlinedSegmentedToggle(
    options: List<String>,
    selectedIndex: Int,
    onOptionSelected: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm),
    ) {
        options.forEachIndexed { index, option ->
            val isSelected = index == selectedIndex

            Box(
                modifier =
                    Modifier
                        .weight(1f)
                        .clip(AppShape.Card)
                        .background(
                            color = if (isSelected) InsColors.Surface else InsColors.Gray100,
                            shape = AppShape.Card,
                        ).then(
                            if (isSelected) {
                                Modifier.border(BorderStroke(1.5.dp, InsColors.Black), AppShape.Card)
                            } else {
                                Modifier
                            },
                        ).selectable(
                            selected = isSelected,
                            onClick = { onOptionSelected(index) },
                            role = Role.Tab,
                        ).padding(vertical = AppSpacing.md),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = option,
                    style =
                        MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        ),
                    color = if (isSelected) InsColors.Black else InsColors.TextSecondary,
                )
            }
        }
    }
}

@Composable
@Preview
private fun OutlinedSegmentedTogglePreview() {
    AppTheme(theme = ThemeType.INSTRUCTOR) {
        OutlinedSegmentedToggle(
            options = listOf("그룹 수업", "개인 수업"),
            selectedIndex = 0,
            onOptionSelected = {},
        )
    }
}
