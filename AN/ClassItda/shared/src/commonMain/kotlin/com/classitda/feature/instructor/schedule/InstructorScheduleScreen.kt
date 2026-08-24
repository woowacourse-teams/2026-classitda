package com.classitda.feature.instructor.schedule

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.classitda.core.designsystem.AppSpacing
import com.classitda.core.designsystem.AppTheme
import com.classitda.core.designsystem.InsColors
import com.classitda.core.designsystem.ThemeType
import com.classitda.core.designsystem.component.TopBar
import com.classitda.domain.model.instructor.management.ClassSession
import com.classitda.domain.model.instructor.management.ClassSessionStatus
import com.classitda.feature.instructor.schedule.component.InstructorCalendar
import com.classitda.feature.instructor.schedule.component.InstructorScheduleCard
import com.classitda.feature.instructor.schedule.component.koreanName
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus
import kotlinx.datetime.number
import kotlinx.datetime.plus
import kotlinx.datetime.todayIn
import org.koin.compose.viewmodel.koinViewModel
import kotlin.time.Clock

@Composable
internal fun InstructorScheduleRoute(
    bottomBar: @Composable () -> Unit,
    onSessionClick: (String) -> Unit = {},
    modifier: Modifier = Modifier,
    viewModel: InstructorScheduleViewModel = koinViewModel(),
) {
    InstructorScheduleStateful(
        bottomBar = bottomBar,
        onSessionClick = onSessionClick,
        modifier = modifier,
        viewModel = viewModel,
    )
}

@Composable
internal fun InstructorScheduleStateful(
    bottomBar: @Composable () -> Unit,
    onSessionClick: (String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: InstructorScheduleViewModel,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val sessions = (uiState as? InstructorScheduleUiState.Success)?.sessions.orEmpty()
    val firstSessionDate = sessions.minOfOrNull { it.startAt.date } ?: LocalDate(2026, 8, 1)
    var displayedYear by remember { mutableStateOf(firstSessionDate.year) }
    var displayedMonth by remember { mutableStateOf(firstSessionDate.month.number) }
    var selectedDate by remember { mutableStateOf(firstSessionDate) }
    var isMonthMode by remember { mutableStateOf(true) }

    LaunchedEffect(sessions) {
        if (sessions.isNotEmpty()) {
            val sessionDate = sessions.minOf { it.startAt.date }
            displayedYear = sessionDate.year
            displayedMonth = sessionDate.month.number
            selectedDate = sessionDate
            isMonthMode = true
        }
    }

    Scaffold(
        modifier = modifier,
        containerColor = InsColors.Background,
        bottomBar = bottomBar,
    ) { contentPadding ->
        when (val state = uiState) {
            InstructorScheduleUiState.Loading -> {
                ScheduleLoading(Modifier.padding(contentPadding))
            }

            is InstructorScheduleUiState.Error -> {
                ScheduleError(
                    message = state.message,
                    onRetry = viewModel::retry,
                    modifier = Modifier.padding(contentPadding),
                )
            }

            is InstructorScheduleUiState.Success -> {
                InstructorScheduleStateless(
                    sessions = state.sessions,
                    displayedYear = displayedYear,
                    displayedMonth = displayedMonth,
                    selectedDate = selectedDate,
                    isMonthMode = isMonthMode,
                    onDateSelected = {
                        selectedDate = it
                        displayedYear = it.year
                        displayedMonth = it.month.number
                    },
                    onModeChange = {
                        isMonthMode = it
                        if (!it) {
                            displayedYear = selectedDate.year
                            displayedMonth = selectedDate.month.number
                        }
                    },
                    onTodayClick = {
                        val today = Clock.System.todayIn(TimeZone.currentSystemDefault())
                        selectedDate = today
                        displayedYear = today.year
                        displayedMonth = today.month.number
                    },
                    onPreviousMonth = {
                        if (!isMonthMode) {
                            selectedDate = selectedDate.minus(DatePeriod(days = 7))
                            displayedYear = selectedDate.year
                            displayedMonth = selectedDate.month.number
                        } else if (displayedMonth == 1) {
                            displayedYear -= 1
                            displayedMonth = 12
                        } else {
                            displayedMonth -= 1
                        }
                    },
                    onNextMonth = {
                        if (!isMonthMode) {
                            selectedDate = selectedDate.plus(DatePeriod(days = 7))
                            displayedYear = selectedDate.year
                            displayedMonth = selectedDate.month.number
                        } else if (displayedMonth == 12) {
                            displayedYear += 1
                            displayedMonth = 1
                        } else {
                            displayedMonth += 1
                        }
                    },
                    onSessionClick = onSessionClick,
                    modifier = Modifier.padding(contentPadding),
                )
            }
        }
    }
}

@Composable
internal fun InstructorScheduleStateless(
    sessions: List<ClassSession>,
    displayedYear: Int,
    displayedMonth: Int,
    selectedDate: LocalDate,
    isMonthMode: Boolean,
    onDateSelected: (LocalDate) -> Unit,
    onModeChange: (Boolean) -> Unit,
    onTodayClick: () -> Unit,
    onPreviousMonth: () -> Unit,
    onNextMonth: () -> Unit,
    onSessionClick: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val selectedSessions = sessions.filter { it.startAt.date == selectedDate }.sortedBy { it.startAt }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = AppSpacing.xxxl),
    ) {
        item {
            TopBar(title = "일정", hasBackground = true)
        }
        item {
            InstructorCalendar(
                displayedYear = displayedYear,
                displayedMonth = displayedMonth,
                selectedDate = selectedDate,
                isMonthMode = isMonthMode,
                scheduledDates =
                    sessions
                        .filter { it.status == ClassSessionStatus.SCHEDULED }
                        .map { it.startAt.date }
                        .toSet(),
                completedDates =
                    sessions
                        .filter { it.status == ClassSessionStatus.COMPLETED }
                        .map { it.startAt.date }
                        .toSet(),
                onDateSelected = onDateSelected,
                onModeChange = onModeChange,
                onTodayClick = onTodayClick,
                onPreviousMonth = onPreviousMonth,
                onNextMonth = onNextMonth,
            )
        }
        item {
            Row(
                Modifier.fillMaxWidth().padding(horizontal = AppSpacing.screenPadding, vertical = AppSpacing.lg),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "${selectedDate.month.number}월 ${selectedDate.day}일 ${selectedDate.dayOfWeek.koreanName}",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                )
                Spacer(Modifier.weight(1f))
                Text(
                    text = "수업 ${selectedSessions.size}개",
                    style = MaterialTheme.typography.bodySmall,
                    color = InsColors.TextSecondary,
                )
            }
        }
        if (selectedSessions.isEmpty()) {
            item {
                Text(
                    "등록된 수업이 없어요",
                    color = InsColors.TextSecondary,
                    modifier = Modifier.padding(horizontal = AppSpacing.screenPadding),
                )
            }
        } else {
            items(selectedSessions, key = { it.id }) { session ->
                InstructorScheduleCard(
                    session = session,
                    onClick = { onSessionClick(session.id) },
                )
            }
        }
    }
}

@Preview(name = "강사 일정", showBackground = true, widthDp = 390, heightDp = 844)
@Composable
private fun InstructorScheduleStatelessPreview() {
    val today = LocalDate(2026, 8, 5)
    var selectedDate by remember { mutableStateOf(today) }
    var isMonthMode by remember { mutableStateOf(true) }

    AppTheme(theme = ThemeType.INSTRUCTOR) {
        InstructorScheduleStateless(
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
                        status = ClassSessionStatus.SCHEDULED,
                    ),
                    ClassSession(
                        id = "2",
                        tags = listOf("개인 수업"),
                        title = "리포머 밸런스",
                        startAt = LocalDateTime(2026, 8, 5, 19, 30),
                        endAt = LocalDateTime(2026, 8, 5, 20, 20),
                        reservedCount = 6,
                        capacity = 6,
                        status = ClassSessionStatus.COMPLETED,
                    ),
                ),
            displayedYear = selectedDate.year,
            displayedMonth = selectedDate.month.number,
            selectedDate = selectedDate,
            isMonthMode = isMonthMode,
            onDateSelected = { selectedDate = it },
            onModeChange = { isMonthMode = it },
            onTodayClick = { selectedDate = today },
            onPreviousMonth = {},
            onNextMonth = {},
            onSessionClick = {},
        )
    }
}

@Composable
private fun ScheduleLoading(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        CircularProgressIndicator(color = InsColors.Primary)
    }
}

@Composable
private fun ScheduleError(
    message: String?,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = message ?: "일정을 불러오지 못했어요",
            color = InsColors.TextSecondary,
        )
        Button(
            onClick = onRetry,
            colors = ButtonDefaults.buttonColors(containerColor = InsColors.Primary),
        ) {
            Text("다시 시도")
        }
    }
}
