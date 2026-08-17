package com.classitda.feature.student.myschedule.component.list

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import classitda.shared.generated.resources.Res
import classitda.shared.generated.resources.my_schedule_tab_history
import classitda.shared.generated.resources.my_schedule_tab_upcoming
import com.classitda.core.designsystem.AppSpacing
import com.classitda.core.designsystem.AppTheme
import com.classitda.core.designsystem.ThemeType
import com.classitda.core.designsystem.appTypography
import com.classitda.feature.student.myschedule.contract.MyScheduleTab
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun MyScheduleTabSelector(
    selectedTab: MyScheduleTab,
    onTabSelected: (MyScheduleTab) -> Unit,
    modifier: Modifier = Modifier,
) {
    val typography = appTypography()
    val tabs = listOf(MyScheduleTab.UPCOMING, MyScheduleTab.HISTORY)

    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surface),
    ) {
        tabs.forEach { tab ->
            val isSelected = selectedTab == tab
            Column(
                modifier =
                    Modifier
                        .weight(1f)
                        .selectable(
                            selected = isSelected,
                            onClick = { onTabSelected(tab) },
                            role = Role.Tab,
                        ),
            ) {
                Box(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .padding(vertical = AppSpacing.xl),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text =
                            stringResource(
                                when (tab) {
                                    MyScheduleTab.UPCOMING -> Res.string.my_schedule_tab_upcoming
                                    MyScheduleTab.HISTORY -> Res.string.my_schedule_tab_history
                                },
                            ),
                        style =
                            typography.titleMedium.copy(
                                fontSize = 15.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            ),
                        color =
                            if (isSelected) {
                                MaterialTheme.colorScheme.onSurface
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            },
                    )
                }
                HorizontalDivider(
                    color =
                        if (isSelected) {
                            MaterialTheme.colorScheme.onSurface
                        } else {
                            MaterialTheme.colorScheme.outlineVariant
                        },
                )
            }
        }
    }
}

@Preview(
    name = "Upcoming · Student · Default",
    group = "Component/MySchedule/Tabs",
    showBackground = true,
    widthDp = 390,
)
@Composable
private fun MyScheduleTabSelectorPreview_Upcoming_Student_Default() {
    AppTheme(theme = ThemeType.STUDENT) {
        MyScheduleTabSelector(
            selectedTab = MyScheduleTab.UPCOMING,
            onTabSelected = {},
        )
    }
}

@Preview(
    name = "History · Student · Default",
    group = "Component/MySchedule/Tabs",
    showBackground = true,
    widthDp = 390,
)
@Composable
private fun MyScheduleTabSelectorPreview_History_Student_Default() {
    AppTheme(theme = ThemeType.STUDENT) {
        MyScheduleTabSelector(
            selectedTab = MyScheduleTab.HISTORY,
            onTabSelected = {},
        )
    }
}
