package com.classitda.feature.instructor.home.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import classitda.shared.generated.resources.Res
import classitda.shared.generated.resources.ic_check
import com.classitda.core.designsystem.AppShape
import com.classitda.core.designsystem.AppSpacing
import com.classitda.core.designsystem.AppTheme
import com.classitda.core.designsystem.InsColors
import com.classitda.core.designsystem.StuColors
import com.classitda.core.designsystem.ThemeType
import com.classitda.domain.model.instructor.management.ClassSession
import com.classitda.domain.model.instructor.management.ClassSessionStatus
import kotlinx.datetime.LocalDateTime
import org.jetbrains.compose.resources.painterResource

@Composable
internal fun InstructorTimeline(
    sessions: List<ClassSession>,
    onSessionClick: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val visibleSessions = sessions.filter { it.status != ClassSessionStatus.CANCELLED }
    val nextSessionIndex = visibleSessions.indexOfFirst { it.status == ClassSessionStatus.SCHEDULED }

    Column(modifier.padding(top = AppSpacing.lg)) {
        visibleSessions.forEachIndexed { index, session ->
            val isNext = session.status == ClassSessionStatus.SCHEDULED && index == nextSessionIndex
            if (isNext) {
                InstructorNextTimelineItem(
                    session = session,
                    isLast = index == visibleSessions.lastIndex,
                    onSessionClick = onSessionClick,
                )
            } else {
                InstructorTimelineItem(
                    session = session,
                    isLast = index == visibleSessions.lastIndex,
                    onSessionClick = onSessionClick,
                )
            }
        }
    }
}

@Composable
private fun InstructorTimelineItem(
    session: ClassSession,
    isLast: Boolean,
    onSessionClick: (String) -> Unit,
) {
    Row(
        Modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min)
            .padding(horizontal = AppSpacing.screenPadding),
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.width(16.dp)) {
            InstructorTimelineIndicator(session.status, isNext = false)
            if (!isLast) {
                Box(Modifier.width(1.dp).fillMaxHeight().background(InsColors.Divider))
            }
        }
        Spacer(Modifier.width(AppSpacing.lg))
        Column(
            modifier = Modifier.padding(bottom = AppSpacing.xl).clickable { onSessionClick(session.id) }.fillMaxWidth(),
        ) {
            Text(
                text = session.instructorTimeText(),
                style = MaterialTheme.typography.bodySmall,
                color = InsColors.TextSecondary,
            )
            Spacer(Modifier.height(AppSpacing.sm))
            Text(
                text = session.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
            )
            Spacer(Modifier.height(AppSpacing.sm))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "예약 ${session.reservedCount}명",
                    color = InsColors.TextSecondary,
                    style = MaterialTheme.typography.bodySmall,
                )
                Spacer(Modifier.width(AppSpacing.sm))
                Text(
                    text = "|",
                    color = InsColors.TextTertiary,
                    style = MaterialTheme.typography.bodySmall,
                )
                Spacer(Modifier.width(AppSpacing.sm))
                Text(
                    text = "정원 ${session.capacity}명",
                    color = InsColors.TextSecondary,
                    style = MaterialTheme.typography.bodySmall,
                )
            }
        }
    }
}

@Composable
private fun InstructorNextTimelineItem(
    session: ClassSession,
    isLast: Boolean,
    onSessionClick: (String) -> Unit,
) {
    Row(
        Modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min)
            .padding(horizontal = AppSpacing.screenPadding),
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.width(16.dp)) {
            InstructorTimelineIndicator(session.status, isNext = true)
            if (!isLast) {
                Box(Modifier.width(1.dp).fillMaxHeight().background(InsColors.Divider))
            }
        }
        Spacer(Modifier.width(AppSpacing.lg))
        Card(
            colors = CardDefaults.cardColors(containerColor = InsColors.White),
            onClick = { onSessionClick(session.id) },
            modifier = Modifier.fillMaxWidth().padding(bottom = AppSpacing.md),
        ) {
            Column(Modifier.padding(AppSpacing.cardPadding).fillMaxWidth()) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = session.instructorTimeText(),
                        style = MaterialTheme.typography.bodySmall,
                        color = InsColors.TextSecondary,
                    )
                    Spacer(Modifier.weight(1f))
                    Surface(shape = AppShape.Pill, color = InsColors.PurpleLight) {
                        Text(
                            text = "다음 수업",
                            color = InsColors.Purple,
                            style = MaterialTheme.typography.labelSmall,
                            modifier = Modifier.padding(horizontal = AppSpacing.sm, vertical = AppSpacing.xs),
                        )
                    }
                }
                Spacer(Modifier.height(AppSpacing.xs))
                Text(
                    text = session.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                )
                HorizontalDivider(modifier = Modifier.padding(vertical = AppSpacing.md), color = StuColors.Divider)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "예약 ${session.reservedCount}명",
                        color = InsColors.TextSecondary,
                        style = MaterialTheme.typography.bodySmall,
                    )
                    Spacer(Modifier.width(AppSpacing.md))
                    Text(
                        text = "정원 ${session.capacity}명",
                        color = InsColors.TextSecondary,
                        style = MaterialTheme.typography.bodySmall,
                    )
                    Spacer(Modifier.weight(1f))
                    Surface(
                        shape = AppShape.Card,
                        color = InsColors.Primary,
                        modifier = Modifier.clickable { onSessionClick(session.id) },
                    ) {
                        Text(
                            text = "수업 상세",
                            color = InsColors.White,
                            style = MaterialTheme.typography.labelMedium,
                            modifier = Modifier.padding(horizontal = AppSpacing.md, vertical = AppSpacing.sm),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun InstructorTimelineIndicator(
    status: ClassSessionStatus,
    isNext: Boolean,
    modifier: Modifier = Modifier,
) {
    when {
        status == ClassSessionStatus.COMPLETED -> {
            Box(
                modifier =
                    modifier
                        .requiredSize(18.dp)
                        .clip(CircleShape)
                        .background(InsColors.Gray400)
                        .border(2.dp, InsColors.White, CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    painter = painterResource(Res.drawable.ic_check),
                    contentDescription = "완료",
                    tint = InsColors.White,
                    modifier = Modifier.requiredSize(12.dp),
                )
            }
        }

        isNext -> {
            Box(
                modifier = modifier.requiredSize(28.dp).clip(CircleShape).background(InsColors.Gray100),
                contentAlignment = Alignment.Center,
            ) {
                Box(
                    modifier =
                        Modifier
                            .requiredSize(18.dp)
                            .clip(CircleShape)
                            .background(InsColors.Black)
                            .border(2.dp, InsColors.White, CircleShape),
                )
            }
        }

        else -> {
            Box(
                modifier =
                    modifier
                        .requiredSize(18.dp)
                        .clip(CircleShape)
                        .background(InsColors.Gray100)
                        .border(2.dp, InsColors.White, CircleShape),
            )
        }
    }
}

internal fun ClassSession.instructorTimeText(): String {
    val startPeriod = startAt.periodText()
    val endPeriod = endAt.periodText()
    val startTime = startAt.clockText()
    val endTime = endAt.clockText()

    return if (startPeriod == endPeriod) {
        "$startPeriod $startTime ~ $endTime"
    } else {
        "$startPeriod $startTime ~ $endPeriod $endTime"
    }
}

private fun LocalDateTime.periodText(): String =
    if (hour < 12) {
        "오전"
    } else {
        "오후"
    }

private fun LocalDateTime.clockText(): String {
    val hourIn12HourFormat = hour % 12
    val displayHour = if (hourIn12HourFormat == 0) 12 else hourIn12HourFormat
    return "$displayHour:${minute.toString().padStart(2, '0')}"
}

private fun previewClassSession() =
    ClassSession(
        id = "preview-session",
        classTypeId = "1",
        tags = listOf("그룹 수업", "필라테스"),
        title = "리포머 밸런스",
        startAt = LocalDateTime(2026, 8, 5, 19, 30),
        endAt = LocalDateTime(2026, 8, 5, 20, 20),
        reservedCount = 6,
        capacity = 8,
        status = ClassSessionStatus.SCHEDULED,
    )

@Preview(name = "타임라인 아이템", showBackground = true, widthDp = 390)
@Composable
private fun InstructorNextTimelineItemPreview() {
    AppTheme(theme = ThemeType.INSTRUCTOR) {
        InstructorNextTimelineItem(
            session = previewClassSession(),
            isLast = false,
            onSessionClick = {},
        )
    }
}

@Preview(name = "일반 타임라인 아이템", showBackground = true, widthDp = 390)
@Composable
private fun InstructorTimelineItemPreview() {
    AppTheme(theme = ThemeType.INSTRUCTOR) {
        InstructorTimelineItem(
            session = previewClassSession(),
            isLast = false,
            onSessionClick = {},
        )
    }
}

@Preview(name = "강사 홈 수업 타임라인", showBackground = true, widthDp = 390)
@Composable
private fun InstructorTimelinePreview() {
    AppTheme(theme = ThemeType.INSTRUCTOR) {
        InstructorTimeline(
            sessions =
                listOf(
                    ClassSession(
                        id = "session-1",
                        classTypeId = "session-1",
                        tags = listOf("그룹 수업", "필라테스"),
                        title = "체어 밸런스",
                        startAt = LocalDateTime(2026, 8, 5, 14, 0),
                        endAt = LocalDateTime(2026, 8, 5, 14, 50),
                        reservedCount = 7,
                        capacity = 8,
                        status = ClassSessionStatus.COMPLETED,
                    ),
                    ClassSession(
                        id = "session-2",
                        classTypeId = "session-2",
                        tags = listOf("그룹 수업", "필라테스"),
                        title = "리포머 밸런스",
                        startAt = LocalDateTime(2026, 8, 5, 19, 30),
                        endAt = LocalDateTime(2026, 8, 5, 20, 20),
                        reservedCount = 6,
                        capacity = 8,
                        status = ClassSessionStatus.SCHEDULED,
                    ),
                    ClassSession(
                        id = "session-3",
                        classTypeId = "session-3",
                        tags = listOf("개인 수업"),
                        title = "바렐 코어 테라피",
                        startAt = LocalDateTime(2026, 8, 5, 20, 30),
                        endAt = LocalDateTime(2026, 8, 5, 21, 20),
                        reservedCount = 2,
                        capacity = 4,
                        status = ClassSessionStatus.SCHEDULED,
                    ),
                ),
            onSessionClick = {},
        )
    }
}
