package com.classitda.feature.instructor.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.classitda.core.designsystem.AppSpacing
import com.classitda.core.designsystem.AppTheme
import com.classitda.core.designsystem.InsColors
import com.classitda.core.designsystem.ThemeType
import com.classitda.domain.model.instructor.management.ClassSession
import com.classitda.domain.model.instructor.management.ClassSessionStatus
import com.classitda.feature.instructor.home.component.InstructorHomeHeader
import com.classitda.feature.instructor.home.component.InstructorHomeSummary
import com.classitda.feature.instructor.home.component.InstructorTimeline
import kotlinx.datetime.LocalDateTime
import org.jetbrains.compose.resources.painterResource
import org.koin.compose.viewmodel.koinViewModel

@Composable
internal fun InstructorHomeRoute(
    onScheduleClick: () -> Unit,
    bottomBar: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: InstructorHomeViewModel = koinViewModel(),
) = InstructorHomeStateful(
    onScheduleClick = onScheduleClick,
    bottomBar = bottomBar,
    modifier = modifier,
    viewModel = viewModel,
)

@Composable
internal fun InstructorHomeStateful(
    onScheduleClick: () -> Unit,
    bottomBar: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: InstructorHomeViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        modifier = modifier,
        containerColor = InsColors.Background,
        bottomBar = bottomBar,
    ) { contentPadding ->
        when (val state = uiState) {
            InstructorHomeUiState.Loading -> LoadingContent(Modifier.padding(contentPadding))
            is InstructorHomeUiState.Error -> ErrorContent(state.message, viewModel::retry, Modifier.padding(contentPadding))
            is InstructorHomeUiState.Success -> InstructorHomeStateless(state.sessions, onScheduleClick, Modifier.padding(contentPadding))
        }
    }
}

@Composable
internal fun InstructorHomeStateless(
    sessions: List<ClassSession>,
    onScheduleClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val visibleSessions = sessions.sortedBy { it.startAt }.take(4)

    LazyColumn(
        modifier = modifier.fillMaxSize().background(InsColors.Background),
        contentPadding = PaddingValues(bottom = AppSpacing.xxxl),
    ) {
        item {
            InstructorHomeHeader()
        }
        item {
            InstructorHomeSummary(visibleSessions)
        }
        item {
            InstructorTimeline(visibleSessions, onScheduleClick)
        }
    }
}

@Composable
private fun LoadingContent(modifier: Modifier = Modifier) {
    Column(modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
        CircularProgressIndicator(color = InsColors.Primary)
    }
}

@Composable
private fun ErrorContent(message: String?, onRetry: () -> Unit, modifier: Modifier = Modifier) {
    Column(modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
        Text(message ?: "일정을 불러오지 못했어요", color = InsColors.TextSecondary)
        Button(onClick = onRetry, colors = ButtonDefaults.buttonColors(containerColor = InsColors.Primary)) { Text("다시 시도") }
    }
}

@Preview(name = "강사 홈 - 수업 있음", showBackground = true, widthDp = 390, heightDp = 844)
@Composable
private fun InstructorHomeStatelessPreview() {
    AppTheme(theme = ThemeType.INSTRUCTOR) {
        InstructorHomeStateless(
            sessions =
                listOf(
                    ClassSession(
                        id = "1",
                        tags = listOf("그룹 수업"),
                        title = "체어 밸런스",
                        startAt = LocalDateTime(2026, 8, 5, 14, 0),
                        endAt = LocalDateTime(2026, 8, 5, 14, 50),
                        reservedCount = 7,
                        capacity = 8,
                        status = ClassSessionStatus.COMPLETED,
                    ),
                    ClassSession(
                        id = "2",
                        tags = listOf("그룹 수업"),
                        title = "리포머 밸런스",
                        startAt = LocalDateTime(2026, 8, 5, 19, 30),
                        endAt = LocalDateTime(2026, 8, 5, 20, 20),
                        reservedCount = 6,
                        capacity = 6,
                        status = ClassSessionStatus.SCHEDULED,
                    ),
                    ClassSession(
                        id = "3",
                        tags = listOf("그룹 수업"),
                        title = "바렐 코어 테라피",
                        startAt = LocalDateTime(2026, 8, 5, 20, 30),
                        endAt = LocalDateTime(2026, 8, 5, 21, 20),
                        reservedCount = 6,
                        capacity = 8,
                        status = ClassSessionStatus.SCHEDULED,
                    ),
                    ClassSession(
                        id = "4",
                        tags = listOf("그룹 수업"),
                        title = "릴렉스 스트레칭",
                        startAt = LocalDateTime(2026, 8, 5, 21, 30),
                        endAt = LocalDateTime(2026, 8, 5, 22, 20),
                        reservedCount = 8,
                        capacity = 8,
                        status = ClassSessionStatus.SCHEDULED,
                    ),
                ),
            onScheduleClick = {},
        )
    }
}
