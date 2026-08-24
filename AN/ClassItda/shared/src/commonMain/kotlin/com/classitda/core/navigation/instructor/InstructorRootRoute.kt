package com.classitda.core.navigation.instructor

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.classitda.feature.instructor.InstructorBottomBar
import com.classitda.feature.instructor.InstructorBottomTab
import com.classitda.feature.instructor.home.InstructorHomeRoute
import com.classitda.feature.instructor.management.lesson.ClassManagementRoute
import com.classitda.feature.instructor.schedule.InstructorScheduleRoute

@Composable
fun InstructorRootRoute(modifier: Modifier = Modifier) {
    var selectedTab by remember { mutableStateOf(InstructorBottomTab.HOME) }

    val bottomBar: @Composable () -> Unit = {
        InstructorBottomBar(
            selectedTab = selectedTab,
            onTabSelected = { selectedTab = it },
        )
    }

    when (selectedTab) {
        InstructorBottomTab.HOME -> {
            InstructorHomeRoute(
                onScheduleClick = { selectedTab = InstructorBottomTab.SCHEDULE },
                bottomBar = bottomBar,
                modifier = modifier,
            )
        }

        InstructorBottomTab.SCHEDULE -> {
            InstructorScheduleRoute(bottomBar = bottomBar, modifier = modifier)
        }

        InstructorBottomTab.MANAGEMENT -> {
            ClassManagementRoute(
                onBackClick = { selectedTab = InstructorBottomTab.HOME },
                onCreateTemplateClick = {},
                onCreateSessionClick = {},
                onTemplateCardClick = {},
                onTemplateEditClick = {},
                onSessionCardClick = {},
                bottomBar = bottomBar,
                modifier = modifier,
            )
        }

        InstructorBottomTab.CHAT,
        InstructorBottomTab.MY,
        -> InstructorHomeRoute(onScheduleClick = {}, bottomBar = bottomBar, modifier = modifier)
    }
}
