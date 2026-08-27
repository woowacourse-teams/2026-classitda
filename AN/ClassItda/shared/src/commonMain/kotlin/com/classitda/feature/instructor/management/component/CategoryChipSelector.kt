package com.classitda.feature.instructor.management.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import com.classitda.core.designsystem.AppShape
import com.classitda.core.designsystem.AppSpacing
import com.classitda.core.designsystem.AppTheme
import com.classitda.core.designsystem.InsColors
import com.classitda.core.designsystem.ThemeType
import com.classitda.domain.model.instructor.management.ClassType

@Composable
internal fun CategoryChipSelector(
    label: String,
    allCategories: List<ClassType>,
    selectedCategory: ClassType?,
    onCategorySelected: (ClassType?) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(AppSpacing.xs),
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
            color = InsColors.TextPrimary,
        )

        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(AppSpacing.xs),
            verticalArrangement = Arrangement.spacedBy(AppSpacing.xs),
        ) {
            allCategories.forEach { category ->
                val isSelected = category == selectedCategory

                Surface(
                    modifier =
                        Modifier.selectable(
                            selected = isSelected,
                            role = Role.RadioButton,
                            onClick = {
                                onCategorySelected(if (isSelected) null else category)
                            },
                        ),
                    shape = AppShape.Pill,
                    color = if (isSelected) InsColors.Primary else InsColors.Gray100,
                    contentColor = if (isSelected) InsColors.White else InsColors.TextSecondary,
                ) {
                    Text(
                        text = category.name,
                        modifier =
                            Modifier.padding(
                                horizontal = AppSpacing.chipHorizontalPadding,
                                vertical = AppSpacing.chipVerticalPadding,
                            ),
                        style = MaterialTheme.typography.labelMedium,
                    )
                }
            }
        }
    }
}

@Composable
@Preview
private fun CategoryChipSelectorPreview() {
    AppTheme(theme = ThemeType.INSTRUCTOR) {
        val categories =
            listOf(
                ClassType(id = "1", name = "필라테스"),
                ClassType(id = "2", name = "요가"),
                ClassType(id = "3", name = "그룹 PT"),
            )
        CategoryChipSelector(
            label = "카테고리 *",
            allCategories = categories,
            selectedCategory = categories.first(),
            onCategorySelected = {},
        )
    }
}
