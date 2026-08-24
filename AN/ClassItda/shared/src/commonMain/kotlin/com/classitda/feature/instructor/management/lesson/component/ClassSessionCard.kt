package com.classitda.feature.instructor.management.lesson.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import com.classitda.core.designsystem.AppShape
import com.classitda.core.designsystem.AppSpacing
import com.classitda.core.designsystem.AppTheme
import com.classitda.core.designsystem.InsColors
import com.classitda.core.designsystem.ThemeType
import com.classitda.domain.model.instructor.management.ClassSessionStatus
import com.classitda.feature.instructor.component.ClassSessionStatusBadge
import com.classitda.feature.instructor.management.lesson.model.ClassSessionUiModel

@Composable
internal fun ClassSessionCard(
    session: ClassSessionUiModel,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier =
            modifier
                .fillMaxWidth()
                .clickable(onClick = onClick)
                .clip(AppShape.Card)
                .background(InsColors.Surface)
                .padding(AppSpacing.cardPadding),
        verticalArrangement = Arrangement.spacedBy(AppSpacing.cardItemVerticalGap),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.spacedBy(AppSpacing.xs),
            ) {
                session.tags.forEach { tag ->
                    Chip(
                        text = tag,
                    )
                }
            }

            ClassSessionStatusBadge(status = session.status)
        }

        Text(
            text = session.title,
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = InsColors.TextPrimary,
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = session.timeRangeText,
                style = MaterialTheme.typography.bodyMedium,
                color = InsColors.TextPrimary,
                modifier = Modifier.weight(1f),
            )
            Text(
                text = "예약 ${session.reservedCount} / 정원 ${session.capacity}",
                style = MaterialTheme.typography.bodyMedium,
                color = InsColors.TextSecondary,
            )
        }
    }
}

@Composable
@Preview
private fun ClassSessionCardPreview() {
    AppTheme(theme = ThemeType.INSTRUCTOR) {
        Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.cardGap)) {
            ClassSessionCard(
                session =
                    ClassSessionUiModel(
                        id = "1",
                        tags = listOf("그룹 수업", "필라테스"),
                        title = "리포머 밸런스",
                        timeRangeText = "오후 7:30 ~ 8:20",
                        reservedCount = 8,
                        capacity = 10,
                        status = ClassSessionStatus.SCHEDULED,
                    ),
                onClick = {},
            )
            ClassSessionCard(
                session =
                    ClassSessionUiModel(
                        id = "2",
                        tags = listOf("그룹 수업", "요가"),
                        title = "하타 요가",
                        timeRangeText = "오전 11:00 ~ 11:50",
                        reservedCount = 8,
                        capacity = 10,
                        status = ClassSessionStatus.CANCELLED,
                    ),
                onClick = {},
            )
            ClassSessionCard(
                session =
                    ClassSessionUiModel(
                        id = "3",
                        tags = listOf("개인 수업", "요가"),
                        title = "리포머 밸런스",
                        timeRangeText = "오전 10:00 ~ 10:50",
                        reservedCount = 1,
                        capacity = 1,
                        status = ClassSessionStatus.COMPLETED,
                    ),
                onClick = {},
            )
        }
    }
}
