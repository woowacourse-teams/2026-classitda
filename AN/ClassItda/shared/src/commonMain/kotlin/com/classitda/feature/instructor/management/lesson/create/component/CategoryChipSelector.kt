package com.classitda.feature.instructor.management.lesson.create.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import classitda.shared.generated.resources.Res
import classitda.shared.generated.resources.ic_add
import classitda.shared.generated.resources.ic_close
import com.classitda.core.designsystem.AppShape
import com.classitda.core.designsystem.AppSpacing
import com.classitda.core.designsystem.AppTheme
import com.classitda.core.designsystem.InsColors
import com.classitda.core.designsystem.ThemeType
import org.jetbrains.compose.resources.painterResource

@Composable
internal fun CategoryChipSelector(
    label: String,
    allCategories: List<String>,
    selectedCategories: List<String>,
    onSelectedCategoriesChanged: (List<String>) -> Unit,
    modifier: Modifier = Modifier,
) {
    var isDialogVisible by remember { mutableStateOf(false) }

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
            selectedCategories.forEach { category ->
                SelectedCategoryChip(
                    text = category,
                    onRemoveClick = { onSelectedCategoriesChanged(selectedCategories - category) },
                )
            }
            AddCategoryChip(onClick = { isDialogVisible = true })
        }
    }

    if (isDialogVisible) {
        CategorySelectDialog(
            allCategories = allCategories,
            selectedCategories = selectedCategories,
            onDismissRequest = { isDialogVisible = false },
            onConfirm = { newSelection ->
                onSelectedCategoriesChanged(newSelection)
                isDialogVisible = false
            },
        )
    }
}

@Composable
private fun SelectedCategoryChip(
    text: String,
    onRemoveClick: () -> Unit,
) {
    Surface(
        shape = AppShape.Pill,
        color = InsColors.Gray100,
        contentColor = InsColors.TextSecondary,
    ) {
        Row(
            modifier =
                Modifier.padding(
                    start = AppSpacing.chipHorizontalPadding,
                    end = AppSpacing.xs,
                    top = AppSpacing.chipVerticalPadding,
                    bottom = AppSpacing.chipVerticalPadding,
                ),
            horizontalArrangement = Arrangement.spacedBy(AppSpacing.xs),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = text,
                style = MaterialTheme.typography.labelMedium,
            )
            Icon(
                painter = painterResource(Res.drawable.ic_close),
                contentDescription = "$text 제거",
                modifier = Modifier.size(16.dp).clickable(onClick = onRemoveClick),
            )
        }
    }
}

@Composable
private fun AddCategoryChip(onClick: () -> Unit) {
    Surface(
        modifier = Modifier.clickable(onClick = onClick),
        shape = AppShape.Pill,
        color = InsColors.Gray100,
        contentColor = InsColors.TextSecondary,
    ) {
        Icon(
            painter = painterResource(Res.drawable.ic_add),
            contentDescription = "카테고리 추가",
            modifier = Modifier.size(32.dp).padding(6.dp),
        )
    }
}

@Composable
@Preview
private fun CategoryChipSelectorPreview() {
    AppTheme(theme = ThemeType.INSTRUCTOR) {
        CategoryChipSelector(
            label = "카테고리 *",
            allCategories = listOf("필라테스", "요가", "그룹 PT"),
            selectedCategories = listOf("필라테스", "요가"),
            onSelectedCategoriesChanged = {},
        )
    }
}
