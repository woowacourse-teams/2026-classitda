package com.classitda.feature.instructor.management.lesson.create.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.window.Dialog
import com.classitda.core.designsystem.AppShape
import com.classitda.core.designsystem.AppSpacing
import com.classitda.core.designsystem.AppTheme
import com.classitda.core.designsystem.InsColors
import com.classitda.core.designsystem.ThemeType
import com.classitda.core.designsystem.component.PrimaryButton

@Composable
internal fun CategorySelectDialog(
    allCategories: List<String>,
    selectedCategories: List<String>,
    onDismissRequest: () -> Unit,
    onConfirm: (List<String>) -> Unit,
) {
    var draftSelection by remember { mutableStateOf(selectedCategories.toSet()) }

    Dialog(onDismissRequest = onDismissRequest) {
        Surface(
            shape = AppShape.Card,
            color = InsColors.Surface,
        ) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(AppSpacing.lg),
                verticalArrangement = Arrangement.spacedBy(AppSpacing.md),
            ) {
                Text(
                    text = "카테고리 선택",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = InsColors.TextPrimary,
                )

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(AppSpacing.xs),
                    verticalArrangement = Arrangement.spacedBy(AppSpacing.xs),
                ) {
                    allCategories.forEach { category ->
                        val isSelected = category in draftSelection

                        Surface(
                            modifier =
                                Modifier.selectable(
                                    selected = isSelected,
                                    role = Role.Checkbox,
                                    onClick = {
                                        draftSelection =
                                            if (isSelected) draftSelection - category else draftSelection + category
                                    },
                                ),
                            shape = AppShape.Pill,
                            color = if (isSelected) InsColors.Primary else InsColors.Gray100,
                            contentColor = if (isSelected) InsColors.White else InsColors.TextSecondary,
                        ) {
                            Text(
                                text = category,
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

                PrimaryButton(
                    text = "완료",
                    onClick = { onConfirm(draftSelection.toList()) },
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    }
}

@Composable
@Preview
private fun CategorySelectDialogPreview() {
    AppTheme(theme = ThemeType.INSTRUCTOR) {
        CategorySelectDialog(
            allCategories = listOf("필라테스", "요가", "그룹 PT"),
            selectedCategories = listOf("필라테스"),
            onDismissRequest = {},
            onConfirm = {},
        )
    }
}
