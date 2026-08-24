package com.classitda.feature.instructor.schedule

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.classitda.core.designsystem.AppShape
import com.classitda.core.designsystem.AppSpacing
import com.classitda.core.designsystem.InsColors
import com.classitda.domain.model.instructor.management.ClassSession
import com.classitda.feature.instructor.management.lesson.component.ClassSessionStatusBadge
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDate
import kotlinx.datetime.YearMonth
import kotlinx.datetime.plus
import kotlinx.datetime.minus
import org.koin.compose.viewmodel.koinViewModel

@Composable
internal fun InstructorScheduleRoute(
    bottomBar: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: InstructorScheduleViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        modifier = modifier,
        containerColor = InsColors.Background,
        bottomBar = bottomBar,
    ) { contentPadding ->
        when (val state = uiState) {
            InstructorScheduleUiState.Loading -> ScheduleLoading(Modifier.padding(contentPadding))
            is InstructorScheduleUiState.Error -> ScheduleError(state.message, viewModel::retry, Modifier.padding(contentPadding))
            is InstructorScheduleUiState.Success -> InstructorScheduleScreen(state.sessions, Modifier.padding(contentPadding))
        }
    }
}

@Composable
private fun InstructorScheduleScreen(
    sessions: List<ClassSession>,
    modifier: Modifier = Modifier,
) {
    val firstSessionDate = sessions.minOfOrNull { it.startAt.date } ?: LocalDate(2026, 8, 1)
    var displayedMonth by remember { mutableStateOf(YearMonth(firstSessionDate.year, firstSessionDate.month)) }
    var selectedDate by remember { mutableStateOf(firstSessionDate) }
    val selectedSessions = sessions.filter { it.startAt.date == selectedDate }.sortedBy { it.startAt }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = AppSpacing.xxxl),
    ) {
        item {
            Column(Modifier.padding(horizontal = AppSpacing.screenPadding, vertical = AppSpacing.lg)) {
                Text("일정", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(AppSpacing.xs))
                Text("수업 일정을 한눈에 확인하세요", color = InsColors.TextSecondary)
            }
        }
        item {
            InstructorCalendar(
                displayedMonth = displayedMonth,
                selectedDate = selectedDate,
                sessionDates = sessions.map { it.startAt.date }.toSet(),
                onDateSelected = { selectedDate = it },
                onPreviousMonth = {
                    displayedMonth = displayedMonth.minus(DatePeriod(months = 1))
                },
                onNextMonth = {
                    displayedMonth = displayedMonth.plus(DatePeriod(months = 1))
                },
            )
        }
        item {
            Text(
                text = "${selectedDate.monthNumber}월 ${selectedDate.dayOfMonth}일 수업",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = AppSpacing.screenPadding, vertical = AppSpacing.lg),
            )
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
    displayedMonth: YearMonth,
    selectedDate: LocalDate,
    sessionDates: Set<LocalDate>,
    onDateSelected: (LocalDate) -> Unit,
    onPreviousMonth: () -> Unit,
    onNextMonth: () -> Unit,
) {
    val firstDay = displayedMonth.firstDay
    val days = buildList<LocalDate?> {
        repeat(firstDay.dayOfWeek.ordinal) { add(null) }
        repeat(displayedMonth.lastDay.dayOfMonth) { add(firstDay.plus(DatePeriod(days = it))) }
        while (size % 7 != 0) add(null)
    }

    Column(
        modifier = Modifier.fillMaxWidth().background(InsColors.Surface).padding(AppSpacing.screenPadding),
        verticalArrangement = Arrangement.spacedBy(AppSpacing.sm),
    ) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text("${displayedMonth.year}년 ${displayedMonth.monthNumber}월", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.weight(1f))
            Text("‹", modifier = Modifier.clickable(onClick = onPreviousMonth).padding(horizontal = AppSpacing.md), style = MaterialTheme.typography.headlineSmall)
            Text("›", modifier = Modifier.clickable(onClick = onNextMonth).padding(horizontal = AppSpacing.md), style = MaterialTheme.typography.headlineSmall)
        }
        Row(Modifier.fillMaxWidth()) {
            listOf("월", "화", "수", "목", "금", "토", "일").forEach { day ->
                Text(day, modifier = Modifier.weight(1f), color = InsColors.TextTertiary, style = MaterialTheme.typography.labelMedium)
            }
        }
        days.chunked(7).forEach { week ->
            Row(Modifier.fillMaxWidth()) {
                week.forEach { date ->
                    CalendarDay(date, date == selectedDate, date in sessionDates, onDateSelected, Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun CalendarDay(
    date: LocalDate?,
    isSelected: Boolean,
    hasSession: Boolean,
    onDateSelected: (LocalDate) -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier.aspectRatio(1f).padding(2.dp).clip(AppShape.Pill).clickable(enabled = date != null) { date?.let(onDateSelected) },
        contentAlignment = Alignment.Center,
    ) {
        if (date != null) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    modifier = Modifier.size(32.dp).clip(AppShape.Pill).background(if (isSelected) InsColors.Primary else Color.Transparent),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(date.dayOfMonth.toString(), color = if (isSelected) InsColors.White else InsColors.TextPrimary)
                }
                if (hasSession) Box(Modifier.size(4.dp).clip(AppShape.Pill).background(InsColors.Purple))
            }
        }
    }
}

@Composable
private fun InstructorScheduleCard(session: ClassSession) {
    Card(
        colors = CardDefaults.cardColors(containerColor = InsColors.Surface),
        modifier = Modifier.padding(horizontal = AppSpacing.screenPadding, vertical = AppSpacing.xs).fillMaxWidth(),
    ) {
        Column(Modifier.padding(AppSpacing.cardPadding)) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text(session.timeText(), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.weight(1f))
                ClassSessionStatusBadge(session.status)
            }
            Spacer(Modifier.height(AppSpacing.sm))
            Text(session.title, style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(AppSpacing.xs))
            Text("예약 ${session.reservedCount} / ${session.capacity}명", color = InsColors.TextSecondary)
        }
    }
}

private fun ClassSession.timeText(): String = "${startAt.hour.toString().padStart(2, '0')}:${startAt.minute.toString().padStart(2, '0')}"

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
