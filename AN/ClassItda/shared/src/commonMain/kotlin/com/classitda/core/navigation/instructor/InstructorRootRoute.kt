package com.classitda.core.navigation.instructor

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.classitda.feature.instructor.InstructorBottomBar
import com.classitda.feature.instructor.InstructorBottomTab
import com.classitda.feature.instructor.classsession.detail.ClassSessionDetailRoute
import com.classitda.feature.instructor.classsession.edit.ClassSessionEditRoute
import com.classitda.feature.instructor.classsession.member.edit.ClassSessionMemberEditRoute
import com.classitda.feature.instructor.home.InstructorHomeRoute
import com.classitda.feature.instructor.management.ManagementFlowNavHost
import com.classitda.feature.instructor.schedule.InstructorScheduleRoute

@Composable
fun InstructorRootRoute(modifier: Modifier = Modifier) {
    var selectedTab by remember { mutableStateOf(InstructorBottomTab.HOME) }
    var selectedSessionId by remember { mutableStateOf<String?>(null) }
    var isSessionEditing by remember { mutableStateOf(false) }
    var isMemberEditing by remember { mutableStateOf(false) }
    var scheduleRefreshKey by remember { mutableStateOf(0) }
    var detailRefreshKey by remember { mutableStateOf(0) }

    val topLevelBottomBar: @Composable () -> Unit = {
        InstructorBottomBar(
            selectedTab = selectedTab,
            onTabSelected = { tab -> selectedTab = tab },
        )
    }

    val sessionId = selectedSessionId
    if (sessionId != null && isMemberEditing) {
        ClassSessionMemberEditRoute(
            sessionId = sessionId,
            onBackClick = { isMemberEditing = false },
            onSaved = {
                isMemberEditing = false
                scheduleRefreshKey++
                detailRefreshKey++
            },
            modifier = modifier,
        )
    } else if (sessionId != null && isSessionEditing) {
        ClassSessionEditRoute(
            sessionId = sessionId,
            onBackClick = { isSessionEditing = false },
            onSaved = {
                isSessionEditing = false
                scheduleRefreshKey++
                detailRefreshKey++
            },
            onDeleted = {
                isSessionEditing = false
                selectedSessionId = null
                selectedTab = InstructorBottomTab.SCHEDULE
                scheduleRefreshKey++
            },
            modifier = modifier,
        )
    } else if (sessionId != null) {
        ClassSessionDetailRoute(
            sessionId = sessionId,
            onBackClick = { selectedSessionId = null },
            onEditClick = { isSessionEditing = true },
            onMemberEditClick = { isMemberEditing = true },
            refreshKey = detailRefreshKey,
            modifier = modifier,
        )
    } else {
        when (selectedTab) {
            InstructorBottomTab.HOME -> {
                InstructorHomeRoute(
                    onSessionClick = { selectedSessionId = it },
                    onStudioChanged = { scheduleRefreshKey++ },
                    bottomBar = {},
                    modifier = modifier,
                )
            }

            InstructorBottomTab.SCHEDULE -> {
                InstructorScheduleRoute(
                    bottomBar = topLevelBottomBar,
                    onSessionClick = { selectedSessionId = it },
                    refreshKey = scheduleRefreshKey,
                    modifier = modifier,
                )
            }

            InstructorBottomTab.MANAGEMENT -> {
                ManagementFlowNavHost(bottomBar = topLevelBottomBar, modifier = modifier)
            }

            InstructorBottomTab.CHAT,
            InstructorBottomTab.MY,
            -> {
                InstructorHomeRoute(
                    onSessionClick = { selectedSessionId = it },
                    onStudioChanged = { scheduleRefreshKey++ },
                    bottomBar = {},
                    modifier = modifier,
                )
            }
        }
    }
}
