package com.classitda.feature.instructor.management.`class`.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.selection.selectable
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import com.classitda.core.designsystem.AppShape
import com.classitda.core.designsystem.AppSpacing
import com.classitda.core.designsystem.AppTheme
import com.classitda.core.designsystem.InsColors
import com.classitda.core.designsystem.ThemeType

internal enum class ClassCategoryFilter(
    val label: String,
) {
    ALL("전체"),
    GROUP("그룹 수업"),
    PERSONAL("개인 수업"),
}

@Composable
internal fun ClassCategoryFilterRow(
    customCategories: List<String>,
    selectedLabel: String,
    onFilterSelected: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val labels =
        remember(customCategories) {
            ClassCategoryFilter.entries.map { it.label } + customCategories
        }

    LazyRow(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm),
        contentPadding = PaddingValues(horizontal = AppSpacing.screenPadding),
    ) {
        items(labels) { label ->
            val isSelected = label == selectedLabel

            FilterChip(
                text = label,
                containerColor = if (isSelected) InsColors.Primary else InsColors.Gray100,
                contentColor = if (isSelected) InsColors.White else InsColors.TextSecondary,
                fontWeight = FontWeight.Normal,
                modifier =
                    Modifier.selectable(
                        selected = isSelected,
                        onClick = { onFilterSelected(label) },
                        role = Role.Tab,
                    ),
            )
        }
    }
}

@Composable
@Preview
private fun ClassCategoryFilterRowPreview() {
    AppTheme(theme = ThemeType.INSTRUCTOR) {
        ClassCategoryFilterRow(
            customCategories = listOf("필라테스", "요가"),
            selectedLabel = ClassCategoryFilter.ALL.label,
            onFilterSelected = {},
        )
    }
}

@Composable
private fun FilterChip(
    text: String,
    containerColor: Color,
    contentColor: Color,
    fontWeight: FontWeight,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier,
        shape = AppShape.Pill,
        color = containerColor,
        contentColor = contentColor,
    ) {
        Text(
            text = text,
            modifier =
                Modifier.padding(
                    horizontal = AppSpacing.chipHorizontalPadding,
                    vertical = AppSpacing.chipVerticalPadding,
                ),
            style = MaterialTheme.typography.labelMedium.copy(fontWeight = fontWeight),
        )
    }
}
