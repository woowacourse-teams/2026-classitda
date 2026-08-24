package com.classitda.feature.instructor.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.classitda.core.designsystem.AppSpacing
import com.classitda.core.designsystem.InsColors
import com.classitda.domain.model.instructor.management.ClassSession
import com.classitda.feature.instructor.InstructorBottomBar
import com.classitda.feature.instructor.InstructorBottomTab
import kotlinx.datetime.LocalDate
import org.koin.compose.viewmodel.koinViewModel

@Composable
internal fun InstructorHomeRoute(
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
            is InstructorHomeUiState.Success -> InstructorHomeScreen(state.sessions, onScheduleClick, Modifier.padding(contentPadding))
        }
    }
}

@Composable
private fun InstructorHomeScreen(
    sessions: List<ClassSession>,
    onScheduleClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val upcomingSessions = sessions.filter { it.status.name != "COMPLETED" }.take(3)

    LazyColumn(
        modifier = modifier.fillMaxSize().background(InsColors.Background),
        contentPadding = PaddingValues(AppSpacing.screenPadding),
        verticalArrangement = Arrangement.spacedBy(AppSpacing.lg),
    ) {
        item {
            Column {
                Text("안녕하세요, 강사님", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(AppSpacing.xs))
                Text("오늘의 수업을 확인해 보세요", color = InsColors.TextSecondary)
            }
        }
        item {
            Card(
                onClick = onScheduleClick,
                colors = CardDefaults.cardColors(containerColor = InsColors.Primary),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column(Modifier.padding(AppSpacing.cardPadding)) {
                    Text("오늘의 일정", color = InsColors.White, style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.height(AppSpacing.sm))
                    Text("예정된 수업 ${upcomingSessions.size}개", color = InsColors.White, style = MaterialTheme.typography.headlineSmall)
                    Spacer(Modifier.height(AppSpacing.sm))
                    Text("일정 전체 보기  ›", color = InsColors.Gray300)
                }
            }
        }
        item {
            Text("예정된 수업", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        }
        items(upcomingSessions, key = { it.id }) { session ->
            InstructorSessionCard(session)
        }
        if (upcomingSessions.isEmpty()) {
            item { Text("예정된 수업이 없어요", color = InsColors.TextSecondary) }
        }
    }
}

@Composable
private fun InstructorSessionCard(session: ClassSession) {
    Card(
        colors = CardDefaults.cardColors(containerColor = InsColors.Surface),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(Modifier.padding(AppSpacing.cardPadding)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(session.dateText(), color = InsColors.TextSecondary, style = MaterialTheme.typography.bodySmall)
                Spacer(Modifier.width(AppSpacing.sm))
                Text(session.timeText(), style = MaterialTheme.typography.bodySmall)
            }
            Spacer(Modifier.height(AppSpacing.sm))
            Text(session.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(AppSpacing.xs))
            Text("예약 ${session.reservedCount} / ${session.capacity}명", color = InsColors.TextSecondary)
        }
    }
}

private fun ClassSession.dateText(): String = "${startAt.date.monthNumber}월 ${startAt.date.dayOfMonth}일"

private fun ClassSession.timeText(): String = "${startAt.hour.toString().padStart(2, '0')}:${startAt.minute.toString().padStart(2, '0')}"

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
