package com.classitda.feature.instructor.management.`class`.component

import androidx.compose.foundation.background
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
import com.classitda.core.designsystem.AppShape
import com.classitda.core.designsystem.AppSpacing
import com.classitda.core.designsystem.AppTheme
import com.classitda.core.designsystem.InsColors
import com.classitda.core.designsystem.ThemeType

internal enum class ClassManagementTopTab(
    val label: String,
) {
    TEMPLATE("수업 템플릿"),
    MY_CLASS("내 수업"),
}

@Composable
internal fun ClassManagementTopTabSelector(
    selectedTab: ClassManagementTopTab,
    onTabSelected: (ClassManagementTopTab) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .background(InsColors.Gray100, AppShape.Card)
                .padding(AppSpacing.xs),
        horizontalArrangement = Arrangement.spacedBy(AppSpacing.xs),
    ) {
        ClassManagementTopTab.entries.forEach { tab ->
            val isSelected = tab == selectedTab

            Box(
                modifier =
                    Modifier
                        .weight(1f)
                        .clip(AppShape.Card)
                        .then(
                            if (isSelected) {
                                Modifier.background(InsColors.White, AppShape.Card)
                            } else {
                                Modifier
                            },
                        ).selectable(
                            selected = isSelected,
                            onClick = { onTabSelected(tab) },
                            role = Role.Tab,
                        ).padding(vertical = AppSpacing.sm),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = tab.label,
                    style =
                        MaterialTheme.typography.titleSmall.copy(
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
private fun ClassManagementTopTabSelectorPreview() {
    AppTheme(theme = ThemeType.INSTRUCTOR) {
        ClassManagementTopTabSelector(
            selectedTab = ClassManagementTopTab.TEMPLATE,
            onTabSelected = {},
            modifier = Modifier.padding(AppSpacing.screenPadding),
        )
    }
}
