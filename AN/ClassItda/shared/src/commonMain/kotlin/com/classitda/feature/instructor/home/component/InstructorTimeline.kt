package com.classitda.feature.instructor.home.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import classitda.shared.generated.resources.ic_close
import com.classitda.core.designsystem.AppShape
import com.classitda.core.designsystem.AppSpacing
import com.classitda.core.designsystem.AppTheme
import com.classitda.core.designsystem.InsColors
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
    val nextSessionIndex = sessions.indexOfFirst { it.status == ClassSessionStatus.SCHEDULED }

    Column(modifier.padding(top = AppSpacing.lg)) {
        sessions.forEachIndexed { index, session ->
            InstructorTimelineItem(
                session = session,
                isNext = session.status == ClassSessionStatus.SCHEDULED && index == nextSessionIndex,
                isLast = index == sessions.lastIndex,
                onSessionClick = onSessionClick,
            )
        }
    }
}

@Composable
private fun InstructorTimelineItem(
    session: ClassSession,
    isNext: Boolean,
    isLast: Boolean,
    onSessionClick: (String) -> Unit,
) {
    Row(Modifier.fillMaxWidth().padding(horizontal = AppSpacing.screenPadding)) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.width(16.dp)) {
            InstructorTimelineIndicator(session.status, isNext)
            if (!isLast) {
                Box(Modifier.width(1.dp).fillMaxHeight().background(InsColors.Divider))
            }
        }
        Spacer(Modifier.width(AppSpacing.md))
        if (isNext) {
            Card(
                colors = CardDefaults.cardColors(containerColor = InsColors.White),
                onClick = { onSessionClick(session.id) },
                modifier = Modifier.fillMaxWidth().padding(bottom = AppSpacing.md),
            ) {
                InstructorTimelineItemContent(
                    session = session,
                    isNext = true,
                    onSessionClick = onSessionClick,
                    modifier = Modifier.padding(AppSpacing.cardPadding),
                )
            }
        } else {
            InstructorTimelineItemContent(
                session = session,
                isNext = false,
                onSessionClick = onSessionClick,
                modifier = Modifier.padding(bottom = AppSpacing.md).clickable { onSessionClick(session.id) },
            )
        }
    }
}

@Composable
private fun InstructorTimelineItemContent(
    session: ClassSession,
    isNext: Boolean,
    onSessionClick: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = session.instructorTimeText(),
                style = MaterialTheme.typography.bodySmall,
                color = InsColors.TextSecondary,
            )
            Spacer(Modifier.weight(1f))
            if (isNext) {
                Surface(shape = AppShape.Pill, color = InsColors.PurpleLight) {
                    Text(
                        text = "다음 수업",
                        color = InsColors.Purple,
                        style = MaterialTheme.typography.labelSmall,
                        modifier =
                            Modifier.padding(
                                horizontal = AppSpacing.sm,
                                vertical = AppSpacing.xs,
                            ),
                    )
                }
            }
        }
        Spacer(Modifier.height(AppSpacing.xs))
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
            Spacer(Modifier.width(AppSpacing.md))
            Text(
                text = "정원 ${session.capacity}명",
                color = InsColors.TextSecondary,
                style = MaterialTheme.typography.bodySmall,
            )
            Spacer(Modifier.weight(1f))
            if (isNext) {
                Surface(
                    shape = AppShape.Card,
                    color = InsColors.Primary,
                    modifier = Modifier.clickable { onSessionClick(session.id) },
                ) {
                    Text(
                        text = "수업 상세",
                        color = InsColors.White,
                        style = MaterialTheme.typography.labelMedium,
                        modifier =
                            Modifier.padding(
                                horizontal = AppSpacing.md,
                                vertical = AppSpacing.sm,
                            ),
                    )
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
                        .size(16.dp)
                        .clip(CircleShape)
                        .background(InsColors.Gray400)
                        .border(2.dp, InsColors.White, CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    painter = painterResource(Res.drawable.ic_check),
                    contentDescription = "완료",
                    tint = InsColors.White,
                    modifier = Modifier.size(10.dp),
                )
            }
        }

        isNext -> {
            Box(
                modifier = modifier.size(24.dp).clip(CircleShape).background(InsColors.Gray100),
                contentAlignment = Alignment.Center,
            ) {
                Box(
                    modifier =
                        Modifier
                            .size(16.dp)
                            .clip(CircleShape)
                            .background(InsColors.Black)
                            .border(2.dp, InsColors.White, CircleShape),
                )
            }
        }

        status == ClassSessionStatus.CANCELLED -> {
            Box(
                modifier =
                    modifier
                        .size(16.dp)
                        .clip(CircleShape)
                        .background(InsColors.Gray100)
                        .border(2.dp, InsColors.White, CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    painter = painterResource(Res.drawable.ic_close),
                    contentDescription = "취소됨",
                    tint = InsColors.Red,
                    modifier = Modifier.size(10.dp),
                )
            }
        }

        else -> {
            Box(
                modifier =
                    modifier
                        .size(16.dp)
                        .clip(CircleShape)
                        .background(InsColors.Gray100)
                        .border(2.dp, InsColors.White, CircleShape),
            )
        }
    }
}

internal fun ClassSession.instructorTimeText(): String =
    "${startAt.hour.toString().padStart(2, '0')}:${startAt.minute.toString().padStart(2, '0')}"

@Preview(name = "강사 홈 수업 타임라인", showBackground = true, widthDp = 390)
@Composable
private fun InstructorTimelinePreview() {
    AppTheme(theme = ThemeType.INSTRUCTOR) {
        InstructorTimeline(
            sessions =
                listOf(
                    ClassSession(
                        id = "session-1",
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
                        tags = listOf("개인 수업"),
                        title = "바렐 코어 테라피",
                        startAt = LocalDateTime(2026, 8, 5, 20, 30),
                        endAt = LocalDateTime(2026, 8, 5, 21, 20),
                        reservedCount = 2,
                        capacity = 4,
                        status = ClassSessionStatus.CANCELLED,
                    ),
                ),
            onSessionClick = {},
        )
    }
}
