package com.classitda.feature.instructor.schedule.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.classitda.core.designsystem.AppSpacing
import com.classitda.core.designsystem.AppTheme
import com.classitda.core.designsystem.InsColors
import com.classitda.core.designsystem.ThemeType
import com.classitda.domain.model.instructor.management.ClassSession
import com.classitda.domain.model.instructor.management.ClassSessionStatus
import com.classitda.feature.instructor.component.ClassSessionStatusBadge
import com.classitda.feature.instructor.component.InstructorClassTagChip
import kotlinx.datetime.LocalDateTime

@Composable
internal fun InstructorScheduleCard(
    session: ClassSession,
    onClick: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    Card(
        onClick = onClick,
        colors = CardDefaults.cardColors(containerColor = InsColors.White),
        modifier = modifier.padding(horizontal = AppSpacing.screenPadding, vertical = AppSpacing.xs).fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier.padding(AppSpacing.cardPadding),
            verticalArrangement = Arrangement.spacedBy(AppSpacing.cardItemVerticalGap),
        ) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Row(horizontalArrangement = Arrangement.spacedBy(AppSpacing.xs)) {
                    session.tags.take(2).forEach { tag ->
                        InstructorClassTagChip(text = tag)
                    }
                }
                Spacer(Modifier.weight(1f))
                ClassSessionStatusBadge(session.status)
            }
            Text(session.title, style = MaterialTheme.typography.titleMedium)
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = session.instructorTimeText(),
                    color = InsColors.TextSecondary,
                    style = MaterialTheme.typography.bodySmall,
                )
                Spacer(Modifier.weight(1f))
                Text(
                    text = "예약 ${session.reservedCount}명  |  정원 ${session.capacity}명",
                    color = InsColors.TextSecondary,
                    style = MaterialTheme.typography.bodySmall,
                )
            }
        }
    }
}

internal fun ClassSession.instructorTimeText(): String {
    val startPeriod = startAt.periodText()
    val endPeriod = endAt.periodText()
    val endText = endAt.clockText()

    return if (startPeriod == endPeriod) {
        "$startPeriod ${startAt.clockText()} ~ $endText"
    } else {
        "$startPeriod ${startAt.clockText()} ~ $endPeriod $endText"
    }
}

private fun LocalDateTime.periodText(): String = if (hour < 12) "오전" else "오후"

private fun LocalDateTime.clockText(): String {
    val displayHour = hour % 12
    val normalizedHour = if (displayHour == 0) 12 else displayHour
    return "$normalizedHour:${minute.toString().padStart(2, '0')}"
}

@Preview(name = "강사 일정 수업 카드", showBackground = true, widthDp = 390)
@Composable
private fun InstructorScheduleCardPreview() {
    AppTheme(theme = ThemeType.INSTRUCTOR) {
        InstructorScheduleCard(
            session =
                ClassSession(
                    id = "session-1",
                    classTypeId = "session-1",
                    tags = listOf("그룹 수업", "필라테스"),
                    title = "리포머 밸런스",
                    startAt = LocalDateTime(2026, 8, 5, 19, 30),
                    endAt = LocalDateTime(2026, 8, 5, 20, 20),
                    reservedCount = 6,
                    capacity = 8,
                    status = ClassSessionStatus.SCHEDULED,
                ),
        )
    }
}
