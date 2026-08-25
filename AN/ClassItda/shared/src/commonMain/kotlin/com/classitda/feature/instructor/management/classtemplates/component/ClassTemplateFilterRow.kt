package com.classitda.feature.instructor.management.classtemplates.component

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
import com.classitda.domain.model.instructor.management.ClassForm
import com.classitda.domain.model.instructor.management.ClassType
import com.classitda.feature.instructor.management.component.CategoryFilter
import com.classitda.feature.instructor.management.component.label

@Composable
internal fun ClassTemplateFilterRow(
    categoryFilters: List<CategoryFilter>,
    selectedFilter: CategoryFilter,
    onFilterSelected: (CategoryFilter) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyRow(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm),
        contentPadding = PaddingValues(horizontal = AppSpacing.screenPadding),
    ) {
        items(categoryFilters) { filter ->
            val isSelected = filter == selectedFilter

            FilterChip(
                text = filter.label,
                containerColor = if (isSelected) InsColors.Primary else InsColors.Gray100,
                contentColor = if (isSelected) InsColors.White else InsColors.TextSecondary,
                fontWeight = FontWeight.Normal,
                modifier =
                    Modifier.selectable(
                        selected = isSelected,
                        onClick = { onFilterSelected(filter) },
                        role = Role.Tab,
                    ),
            )
        }
    }
}

@Composable
@Preview
private fun ClassTemplateFilterRowPreview() {
    AppTheme(theme = ThemeType.INSTRUCTOR) {
        ClassTemplateFilterRow(
            categoryFilters =
                listOf(
                    CategoryFilter.All,
                    CategoryFilter.Form(ClassForm.INDIVIDUAL),
                    CategoryFilter.Form(ClassForm.GROUP),
                    CategoryFilter.Category(ClassType(id = "1", name = "필라테스")),
                    CategoryFilter.Category(ClassType(id = "2", name = "요가")),
                ),
            selectedFilter = CategoryFilter.All,
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
