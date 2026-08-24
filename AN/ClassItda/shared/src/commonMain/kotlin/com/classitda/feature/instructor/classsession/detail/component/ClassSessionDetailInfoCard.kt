package com.classitda.feature.instructor.classsession.detail.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import com.classitda.core.designsystem.AppShape
import com.classitda.core.designsystem.AppSpacing
import com.classitda.core.designsystem.AppTheme
import com.classitda.core.designsystem.InsColors
import com.classitda.core.designsystem.ThemeType
import com.classitda.domain.model.instructor.management.ClassSessionStatus
import com.classitda.feature.instructor.classsession.detail.model.ClassSessionDetailUiModel

@Composable
internal fun ClassSessionDetailInfoCard(
    detail: ClassSessionDetailUiModel,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = AppShape.Card,
        color = InsColors.White,
    ) {
        Column(
            modifier = Modifier.padding(AppSpacing.cardPadding),
            verticalArrangement = Arrangement.spacedBy(AppSpacing.sm),
        ) {
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(AppSpacing.xs),
                verticalArrangement = Arrangement.spacedBy(AppSpacing.xs),
            ) {
                detail.tags.forEach { tag ->
                    Surface(
                        shape = AppShape.Pill,
                        color = InsColors.SurfaceVariant,
                    ) {
                        Text(
                            text = tag,
                            color = InsColors.TextSecondary,
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
            Text(
                text = detail.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = InsColors.TextPrimary,
            )
            Text(
                text = detail.timeText,
                style = MaterialTheme.typography.bodyMedium,
                color = InsColors.TextPrimary,
            )
            Spacer(modifier = Modifier.height(AppSpacing.xs))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(AppSpacing.xxl),
            ) {
                DetailStat(label = "예약 인원", value = "${detail.reservedCount}명")
                DetailStat(label = "정원", value = "${detail.capacity}명")
            }
            DetailDescription(label = "설명", value = detail.description)
            DetailDescription(label = "장소", value = detail.location)
        }
    }
}

@Composable
private fun DetailStat(
    label: String,
    value: String,
) {
    Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.xs)) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = InsColors.TextTertiary,
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            color = InsColors.TextPrimary,
        )
    }
}

@Composable
private fun DetailDescription(
    label: String,
    value: String,
) {
    Row(horizontalArrangement = Arrangement.spacedBy(AppSpacing.lg)) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = InsColors.TextTertiary,
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall,
            color = InsColors.TextSecondary,
        )
    }
}

@Preview(name = "수업 상세 정보 카드", showBackground = true, widthDp = 350)
@Composable
private fun ClassSessionDetailInfoCardPreview() {
    AppTheme(theme = ThemeType.INSTRUCTOR) {
        ClassSessionDetailInfoCard(
            detail =
                ClassSessionDetailUiModel(
                    id = "session-1",
                    dateText = "2026.08.05 (수)",
                    tags = listOf("그룹 수업", "필라테스"),
                    title = "리포머 밸런스",
                    timeText = "오후 7:30 ~ 8:40",
                    reservedCount = 3,
                    capacity = 8,
                    description = "체어룸에서 할 예정",
                    location = "체어룸",
                    status = ClassSessionStatus.SCHEDULED,
                    members = emptyList(),
                ),
        )
    }
}
