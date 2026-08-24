package com.classitda.feature.instructor.schedule

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import classitda.shared.generated.resources.Res
import classitda.shared.generated.resources.ic_arrow_back
import classitda.shared.generated.resources.ic_arrow_forward
import com.classitda.core.designsystem.AppShape
import com.classitda.core.designsystem.AppSpacing
import com.classitda.core.designsystem.AppTheme
import com.classitda.core.designsystem.InsColors
import com.classitda.core.designsystem.ThemeType
import com.classitda.core.designsystem.component.TopBar
import com.classitda.domain.model.instructor.management.ClassSession
import com.classitda.domain.model.instructor.management.ClassSessionStatus
import com.classitda.feature.instructor.management.lesson.component.ClassSessionStatusBadge
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.Month
import kotlinx.datetime.YearMonth
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus
import kotlinx.datetime.number
import kotlinx.datetime.plus
import kotlinx.datetime.todayIn
import org.jetbrains.compose.resources.painterResource
import org.koin.compose.viewmodel.koinViewModel
import kotlin.time.Clock

@Composable
internal fun InstructorScheduleRoute(
    bottomBar: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: InstructorScheduleViewModel = koinViewModel(),
) {
    InstructorScheduleStateful(
        bottomBar = bottomBar,
        modifier = modifier,
        viewModel = viewModel,
    )
}

@Composable
internal fun InstructorScheduleStateful(
    bottomBar: @Composable () -> Unit,
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
            InstructorScheduleUiState.Loading -> ScheduleLoading(Modifier.padding(contentPadding))
            is InstructorScheduleUiState.Error -> ScheduleError(state.message, viewModel::retry, Modifier.padding(contentPadding))
            is InstructorScheduleUiState.Success -> InstructorScheduleStateless(
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
                onModeChange = { isMonthMode = it },
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
                modifier = Modifier.padding(contentPadding),
            )
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
                scheduledDates = sessions.filter { it.status == ClassSessionStatus.SCHEDULED }.map { it.startAt.date }.toSet(),
                completedDates = sessions.filter { it.status == ClassSessionStatus.COMPLETED }.map { it.startAt.date }.toSet(),
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
                Text("수업 ${selectedSessions.size}개", style = MaterialTheme.typography.bodySmall, color = InsColors.TextSecondary)
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
            items(selectedSessions, key = { it.id }) { session -> InstructorScheduleCard(session) }
        }
    }
}

@Composable
private fun InstructorCalendar(
    displayedYear: Int,
    displayedMonth: Int,
    selectedDate: LocalDate,
    isMonthMode: Boolean,
    scheduledDates: Set<LocalDate>,
    completedDates: Set<LocalDate>,
    onDateSelected: (LocalDate) -> Unit,
    onModeChange: (Boolean) -> Unit,
    onTodayClick: () -> Unit,
    onPreviousMonth: () -> Unit,
    onNextMonth: () -> Unit,
) {
    val calendarMonth = YearMonth(displayedYear, Month.entries[displayedMonth - 1])
    val firstDay = calendarMonth.firstDay
    val monthDays = buildList<LocalDate?> {
        repeat(firstDay.dayOfWeek.ordinal) { add(null) }
        repeat(calendarMonth.lastDay.day) { add(firstDay.plus(DatePeriod(days = it))) }
        while (size % 7 != 0) add(null)
    }
    val weekStart = selectedDate.minus(DatePeriod(days = selectedDate.dayOfWeek.ordinal))
    val weekDays = List(7) { index -> weekStart.plus(DatePeriod(days = index)) }

    Column(
        modifier = Modifier.fillMaxWidth().background(InsColors.White).padding(AppSpacing.screenPadding),
        verticalArrangement = Arrangement.spacedBy(AppSpacing.sm),
    ) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Icon(
                painter = painterResource(Res.drawable.ic_arrow_back),
                contentDescription = "이전 달",
                tint = InsColors.TextSecondary,
                modifier = Modifier.size(20.dp).clickable(onClick = onPreviousMonth),
            )
            Spacer(Modifier.width(AppSpacing.sm))
            Text("${displayedYear}년 ${displayedMonth}월", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.width(AppSpacing.sm))
            Icon(
                painter = painterResource(Res.drawable.ic_arrow_forward),
                contentDescription = "다음 달",
                tint = InsColors.TextSecondary,
                modifier = Modifier.size(20.dp).clickable(onClick = onNextMonth),
            )
            Spacer(Modifier.weight(1f))
            Surface(shape = AppShape.Card, color = InsColors.SurfaceVariant) {
                Row(Modifier.padding(2.dp)) {
                    Surface(
                        shape = AppShape.Card,
                        color = if (isMonthMode) InsColors.White else Color.Transparent,
                        modifier = Modifier.clickable { onModeChange(true) },
                    ) {
                        Text("월", color = if (isMonthMode) InsColors.TextPrimary else InsColors.TextSecondary, style = MaterialTheme.typography.labelSmall, modifier = Modifier.padding(horizontal = AppSpacing.sm, vertical = AppSpacing.xs))
                    }
                    Surface(
                        shape = AppShape.Card,
                        color = if (isMonthMode) Color.Transparent else InsColors.White,
                        modifier = Modifier.clickable { onModeChange(false) },
                    ) {
                        Text("주", color = if (isMonthMode) InsColors.TextSecondary else InsColors.TextPrimary, style = MaterialTheme.typography.labelSmall, modifier = Modifier.padding(horizontal = AppSpacing.sm, vertical = AppSpacing.xs))
                    }
                }
            }
        }
        Row(Modifier.fillMaxWidth()) {
            listOf("월", "화", "수", "목", "금", "토", "일").forEach { day ->
                Text(day, modifier = Modifier.weight(1f), color = InsColors.TextTertiary, style = MaterialTheme.typography.labelSmall, textAlign = TextAlign.Center)
            }
        }
        val visibleWeeks: List<List<LocalDate?>> = if (isMonthMode) monthDays.chunked(7) else listOf(weekDays)
        visibleWeeks.forEach { week ->
            Row(Modifier.fillMaxWidth()) {
                week.forEach { date ->
                    CalendarDay(
                        date = date,
                        isSelected = date == selectedDate,
                        hasScheduledSession = date in scheduledDates,
                        hasCompletedSession = date in completedDates,
                        onDateSelected = onDateSelected,
                        modifier = Modifier.weight(1f),
                    )
                }
            }
        }
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            CalendarLegendDot(InsColors.Purple)
            Text("예정 수업", color = InsColors.TextSecondary, style = MaterialTheme.typography.bodySmall)
            Spacer(Modifier.width(AppSpacing.xxl))
            CalendarLegendDot(InsColors.Gray400)
            Text("완료 수업", color = InsColors.TextSecondary, style = MaterialTheme.typography.bodySmall)
            Spacer(Modifier.weight(1f))
            Text("오늘", color = InsColors.TextSecondary, style = MaterialTheme.typography.bodySmall, modifier = Modifier.clickable(onClick = onTodayClick))
        }
    }
}

@Composable
private fun CalendarDay(
    date: LocalDate?,
    isSelected: Boolean,
    hasScheduledSession: Boolean,
    hasCompletedSession: Boolean,
    onDateSelected: (LocalDate) -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier.height(40.dp).clip(AppShape.Pill).clickable(enabled = date != null) { date?.let(onDateSelected) },
        contentAlignment = Alignment.Center,
    ) {
        if (date != null) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    modifier = Modifier.size(28.dp).clip(AppShape.Pill).background(if (isSelected) InsColors.Black else Color.Transparent),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(date.day.toString(), color = if (isSelected) InsColors.White else InsColors.TextPrimary)
                }
                Row(Modifier.height(6.dp), horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                    if (hasScheduledSession) CalendarLegendDot(InsColors.Purple)
                    if (hasCompletedSession) CalendarLegendDot(InsColors.Gray400)
                }
            }
        }
    }
}

@Composable
private fun CalendarLegendDot(color: Color) {
    Box(Modifier.size(4.dp).clip(AppShape.Pill).background(color))
}

@Composable
private fun InstructorScheduleCard(session: ClassSession) {
    Card(
        colors = CardDefaults.cardColors(containerColor = InsColors.White),
        modifier = Modifier.padding(horizontal = AppSpacing.screenPadding, vertical = AppSpacing.xs).fillMaxWidth(),
    ) {
        Column(Modifier.padding(AppSpacing.cardPadding)) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Row(horizontalArrangement = Arrangement.spacedBy(AppSpacing.xs)) {
                    session.tags.take(2).forEach { tag ->
                        Surface(shape = AppShape.Pill, color = InsColors.SurfaceVariant) {
                            Text(
                                text = tag,
                                color = InsColors.TextSecondary,
                                style = MaterialTheme.typography.labelSmall,
                                modifier = Modifier.padding(horizontal = AppSpacing.sm, vertical = AppSpacing.xs),
                            )
                        }
                    }
                }
                Spacer(Modifier.weight(1f))
                ClassSessionStatusBadge(session.status)
            }
            Spacer(Modifier.height(AppSpacing.sm))
            Text(session.title, style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(AppSpacing.xs))
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text(session.timeText(), color = InsColors.TextSecondary, style = MaterialTheme.typography.bodySmall)
                Spacer(Modifier.weight(1f))
                Text("예약 ${session.reservedCount}명  |  정원 ${session.capacity}명", color = InsColors.TextSecondary, style = MaterialTheme.typography.bodySmall)
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
        )
    }
}

private fun ClassSession.timeText(): String = "${startAt.hour.toString().padStart(2, '0')}:${startAt.minute.toString().padStart(2, '0')}"

private val DayOfWeek.koreanName: String
    get() =
        when (this) {
            DayOfWeek.MONDAY -> "월요일"
            DayOfWeek.TUESDAY -> "화요일"
            DayOfWeek.WEDNESDAY -> "수요일"
            DayOfWeek.THURSDAY -> "목요일"
            DayOfWeek.FRIDAY -> "금요일"
            DayOfWeek.SATURDAY -> "토요일"
            DayOfWeek.SUNDAY -> "일요일"
        }

@Composable
private fun ScheduleLoading(modifier: Modifier = Modifier) {
    Box(modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator(color = InsColors.Primary) }
}

@Composable
private fun ScheduleError(message: String?, onRetry: () -> Unit, modifier: Modifier = Modifier) {
    Column(modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
        Text(message ?: "일정을 불러오지 못했어요", color = InsColors.TextSecondary)
        Button(onClick = onRetry, colors = ButtonDefaults.buttonColors(containerColor = InsColors.Primary)) { Text("다시 시도") }
    }
}
