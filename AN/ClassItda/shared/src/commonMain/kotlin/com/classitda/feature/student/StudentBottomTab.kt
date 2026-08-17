package com.classitda.feature.student

import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import classitda.shared.generated.resources.Res
import classitda.shared.generated.resources.ic_calendar
import classitda.shared.generated.resources.ic_chat
import classitda.shared.generated.resources.ic_home
import classitda.shared.generated.resources.ic_person
import classitda.shared.generated.resources.ic_schedule_calendar
import com.classitda.core.designsystem.AppTheme
import com.classitda.core.designsystem.StuColors
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource

enum class StudentTab {
    HOME,
    RESERVATION,
    MY_SCHEDULE,
    MESSAGE,
    MYPage,
}

private val StudentTab.label: String
    get() =
        when (this) {
            StudentTab.HOME -> "홈"
            StudentTab.RESERVATION -> "예약"
            StudentTab.MY_SCHEDULE -> "내일정"
            StudentTab.MESSAGE -> "메시지"
            StudentTab.MYPage -> "마이"
        }

private val StudentTab.icon: DrawableResource
    get() =
        when (this) {
            StudentTab.HOME -> Res.drawable.ic_home
            StudentTab.RESERVATION -> Res.drawable.ic_schedule_calendar
            StudentTab.MY_SCHEDULE -> Res.drawable.ic_calendar
            StudentTab.MESSAGE -> Res.drawable.ic_chat
            StudentTab.MYPage -> Res.drawable.ic_person
        }

@Composable
fun StudentBottomTab(
    selectedTab: StudentTab,
    onTabSelected: (StudentTab) -> Unit,
    modifier: Modifier = Modifier,
) {
    NavigationBar(
        modifier = modifier,
        containerColor = StuColors.Surface,
    ) {
        StudentTab.entries.forEach { tab ->
            NavigationBarItem(
                selected = tab == selectedTab,
                onClick = { onTabSelected(tab) },
                icon = {
                    Icon(
                        painter = painterResource(tab.icon),
                        contentDescription = tab.label,
                        modifier = Modifier.size(24.dp),
                    )
                },
                label = {
                    Text(text = tab.label)
                },
                colors =
                    NavigationBarItemDefaults.colors(
                        selectedIconColor = StuColors.Primary,
                        selectedTextColor = StuColors.Primary,
                        unselectedIconColor = StuColors.TextTertiary,
                        unselectedTextColor = StuColors.TextTertiary,
                        indicatorColor = Color.Transparent,
                    ),
            )
        }
    }
}

@Composable
@Preview
private fun StudentBottomTabPreview() {
    AppTheme {
        StudentBottomTab(
            selectedTab = StudentTab.HOME,
            onTabSelected = {},
        )
    }
}
