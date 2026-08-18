package com.classitda.core.navigation.student

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.classitda.core.designsystem.StuColors
import com.classitda.core.navigation.reservation.ReservationNavHost
import com.classitda.feature.student.StudentBottomTab
import com.classitda.feature.student.StudentTab
import com.classitda.feature.student.home.HomeScreen
import com.classitda.feature.student.myschedule.MyScheduleFlowRoute

@Composable
fun StudentRootRoute(modifier: Modifier = Modifier) {
    var selectedTab by remember { mutableStateOf(StudentTab.HOME) }

    val bottomBar: @Composable () -> Unit = {
        StudentBottomTab(
            selectedTab = selectedTab,
            onTabSelected = { selectedTab = it },
        )
    }

    when (selectedTab) {
        StudentTab.HOME -> {
            HomeScreen(
                onTabSelected = { selectedTab = it },
                bottomBar = bottomBar,
            )
        }

        StudentTab.RESERVATION -> {
            ReservationNavHost(
                modifier = modifier,
                bottomBar = bottomBar,
            )
        }

        StudentTab.MY_SCHEDULE -> {
            MyScheduleFlowRoute(
                modifier = modifier,
                bottomBar = bottomBar,
            )
        }

        StudentTab.MESSAGE, StudentTab.MYPage -> {
            StudentPlaceholderRoute(bottomBar = bottomBar)
        }
    }
}

@Composable
private fun StudentPlaceholderRoute(bottomBar: @Composable () -> Unit) {
    Scaffold(
        containerColor = StuColors.Background,
        bottomBar = bottomBar,
    ) { innerPadding ->
        Box(
            modifier = Modifier.fillMaxSize().padding(innerPadding),
            contentAlignment = Alignment.Center,
        ) {
            Text(text = "준비 중인 화면이에요", color = StuColors.TextSecondary)
        }
    }
}
