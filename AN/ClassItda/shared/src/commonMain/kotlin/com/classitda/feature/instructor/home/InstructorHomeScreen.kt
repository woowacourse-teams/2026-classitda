package com.classitda.feature.instructor.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Card
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import classitda.shared.generated.resources.Res
import classitda.shared.generated.resources.ic_notification
import com.classitda.core.designsystem.AppSpacing
import com.classitda.core.designsystem.AppShape
import com.classitda.core.designsystem.InsColors
import com.classitda.domain.model.instructor.management.ClassSession
import com.classitda.domain.model.instructor.management.ClassSessionStatus
import org.jetbrains.compose.resources.painterResource
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
    val visibleSessions = sessions.sortedBy { it.startAt }.take(4)
    val remainingCount = visibleSessions.count { it.status == ClassSessionStatus.SCHEDULED }

    LazyColumn(
        modifier = modifier.fillMaxSize().background(InsColors.Background),
        contentPadding = PaddingValues(bottom = AppSpacing.xxxl),
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = AppSpacing.screenPadding, vertical = AppSpacing.lg),
                verticalAlignment = Alignment.Top,
            ) {
                Column(Modifier.weight(1f)) {
                    Text("안녕하세요, 이지은 강사님", color = InsColors.TextSecondary, style = MaterialTheme.typography.bodyMedium)
                    Spacer(Modifier.height(AppSpacing.xs))
                    Text("클래스잇다 요가&필라테스⌄", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                }
                IconButton(onClick = {}) {
                    Icon(
                        painter = painterResource(Res.drawable.ic_notification),
                        contentDescription = "알림",
                        tint = InsColors.TextPrimary,
                    )
                }
            }
        }
        item {
            Column(Modifier.padding(horizontal = AppSpacing.screenPadding)) {
                Text("오늘 수업 ${visibleSessions.size}개", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(AppSpacing.xs))
                Text("완료 ${visibleSessions.count { it.status == ClassSessionStatus.COMPLETED }}개 · 남은 수업 ${remainingCount}개", color = InsColors.TextSecondary)
            }
        }
        item {
            Column(Modifier.padding(top = AppSpacing.lg)) {
                visibleSessions.forEachIndexed { index, session ->
                    InstructorTimelineItem(
                        session = session,
                        isNext = session.status == ClassSessionStatus.SCHEDULED && index == visibleSessions.indexOfFirst { it.status == ClassSessionStatus.SCHEDULED },
                        isLast = index == visibleSessions.lastIndex,
                        onScheduleClick = onScheduleClick,
                    )
                }
            }
        }
    }
}

@Composable
private fun InstructorTimelineItem(
    session: ClassSession,
    isNext: Boolean,
    isLast: Boolean,
    onScheduleClick: () -> Unit,
) {
    Row(Modifier.fillMaxWidth().padding(horizontal = AppSpacing.screenPadding)) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.width(24.dp)) {
            Spacer(Modifier.height(AppSpacing.sm))
            Box(
                modifier = Modifier.size(12.dp).clip(CircleShape).background(if (isNext) InsColors.Black else InsColors.Gray200),
            )
            if (!isLast) {
                Box(Modifier.width(1.dp).fillMaxHeight().background(InsColors.Divider))
            }
        }
        Spacer(Modifier.width(AppSpacing.md))
        Card(
            onClick = onScheduleClick,
            colors = CardDefaults.cardColors(containerColor = InsColors.Surface),
            modifier = Modifier.fillMaxWidth().padding(bottom = AppSpacing.md),
        ) {
            Column(Modifier.padding(AppSpacing.cardPadding)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(session.timeText(), style = MaterialTheme.typography.bodySmall, color = InsColors.TextSecondary)
                    Spacer(Modifier.weight(1f))
                    if (isNext) {
                        Surface(shape = AppShape.Pill, color = InsColors.PurpleLight) {
                            Text("다음 수업", color = InsColors.Purple, style = MaterialTheme.typography.labelSmall, modifier = Modifier.padding(horizontal = AppSpacing.sm, vertical = AppSpacing.xs))
                        }
                    }
                }
                Spacer(Modifier.height(AppSpacing.xs))
                Text(session.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(AppSpacing.sm))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("예약 ${session.reservedCount}명", color = InsColors.TextSecondary, style = MaterialTheme.typography.bodySmall)
                    Spacer(Modifier.width(AppSpacing.md))
                    Text("정원 ${session.capacity}명", color = InsColors.TextSecondary, style = MaterialTheme.typography.bodySmall)
                    Spacer(Modifier.weight(1f))
                    if (isNext) {
                        Surface(shape = AppShape.Card, color = InsColors.Primary, modifier = Modifier.clickable(onClick = onScheduleClick)) {
                            Text("수업 상세", color = InsColors.White, style = MaterialTheme.typography.labelMedium, modifier = Modifier.padding(horizontal = AppSpacing.md, vertical = AppSpacing.sm))
                        }
                    }
                }
            }
        }
    }
}

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
