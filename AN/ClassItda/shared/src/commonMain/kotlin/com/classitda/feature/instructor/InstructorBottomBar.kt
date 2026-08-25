package com.classitda.feature.instructor

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
import classitda.shared.generated.resources.ic_check
import classitda.shared.generated.resources.ic_home
import classitda.shared.generated.resources.ic_person
import classitda.shared.generated.resources.ic_schedule_calendar
import com.classitda.core.designsystem.AppTheme
import com.classitda.core.designsystem.InsColors
import com.classitda.core.designsystem.ThemeType
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource

internal enum class InstructorBottomTab {
    HOME,
    SCHEDULE,
    MANAGEMENT,
    CHAT,
    MY,
}

private val InstructorBottomTab.label: String
    get() =
        when (this) {
            InstructorBottomTab.HOME -> "홈"
            InstructorBottomTab.SCHEDULE -> "일정"
            InstructorBottomTab.MANAGEMENT -> "관리"
            InstructorBottomTab.CHAT -> "메시지"
            InstructorBottomTab.MY -> "마이"
        }

private val InstructorBottomTab.icon: DrawableResource
    get() =
        when (this) {
            InstructorBottomTab.HOME -> Res.drawable.ic_home
            InstructorBottomTab.SCHEDULE -> Res.drawable.ic_calendar
            InstructorBottomTab.MANAGEMENT -> Res.drawable.ic_schedule_calendar
            InstructorBottomTab.CHAT -> Res.drawable.ic_chat
            InstructorBottomTab.MY -> Res.drawable.ic_person
        }

@Composable
internal fun InstructorBottomBar(
    selectedTab: InstructorBottomTab,
    onTabSelected: (InstructorBottomTab) -> Unit,
    modifier: Modifier = Modifier,
) {
    NavigationBar(
        modifier = modifier,
        containerColor = InsColors.Surface,
    ) {
        InstructorBottomTab.entries.forEach { tab ->
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
                        selectedIconColor = InsColors.Purple,
                        selectedTextColor = InsColors.Purple,
                        unselectedIconColor = InsColors.TextTertiary,
                        unselectedTextColor = InsColors.TextTertiary,
                        indicatorColor = Color.Transparent,
                    ),
            )
        }
    }
}

@Composable
@Preview
private fun InstructorBottomBarPreview() {
    AppTheme(theme = ThemeType.INSTRUCTOR) {
        InstructorBottomBar(
            selectedTab = InstructorBottomTab.MANAGEMENT,
            onTabSelected = {},
        )
    }
}
