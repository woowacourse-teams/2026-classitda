package com.classitda.feature.student.myschedule.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import classitda.shared.generated.resources.Res
import classitda.shared.generated.resources.my_schedule_tab_history
import classitda.shared.generated.resources.my_schedule_tab_upcoming
import com.classitda.core.designsystem.AppSpacing
import com.classitda.core.designsystem.AppTheme
import com.classitda.core.designsystem.ThemeType
import com.classitda.feature.student.myschedule.contract.MyScheduleTab
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun MyScheduleTabRow(
    selectedTab: MyScheduleTab,
    modifier: Modifier = Modifier,
) {
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
                        .weight(1f),
            ) {
                Box(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .padding(vertical = AppSpacing.md),
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
                            MaterialTheme.typography.titleMedium.copy(
                                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                            ),
                        color =
                            if (isSelected) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            },
                    )
                }
                HorizontalDivider(
                    color =
                        if (isSelected) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.outline
                        },
                )
            }
        }
    }
}

@Preview(
    name = "Tabs / Upcoming / Student",
    group = "Component/MySchedule",
    showBackground = true,
    widthDp = 390,
)
@Composable
private fun `MyScheduleTabRowPreview_Upcoming_STUDENT_Default`() {
    AppTheme(theme = ThemeType.STUDENT) {
        MyScheduleTabRow(
            selectedTab = MyScheduleTab.UPCOMING,
        )
    }
}

@Preview(
    name = "Tabs · History · Student",
    group = "Component/MySchedule",
    showBackground = true,
    widthDp = 390,
)
@Composable
private fun MyScheduleTabRowPreview_History_Student_Default() {
    AppTheme(theme = ThemeType.STUDENT) {
        MyScheduleTabRow(
            selectedTab = MyScheduleTab.HISTORY,
        )
    }
}
