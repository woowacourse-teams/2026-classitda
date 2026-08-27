package com.classitda.feature.instructor.classsession.detail.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp
import com.classitda.core.designsystem.AppShape
import com.classitda.core.designsystem.AppSpacing
import com.classitda.core.designsystem.AppTheme
import com.classitda.core.designsystem.InsColors
import com.classitda.core.designsystem.ThemeType
import com.classitda.domain.model.instructor.management.ClassSessionStatus
import com.classitda.feature.instructor.classsession.detail.model.ClassSessionDetailUiModel
import com.classitda.feature.instructor.component.InstructorClassTagChip

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
            verticalArrangement = Arrangement.spacedBy(AppSpacing.cardItemVerticalGap),
        ) {
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm),
                verticalArrangement = Arrangement.spacedBy(AppSpacing.sm),
            ) {
                detail.tags.forEach { tag ->
                    InstructorClassTagChip(text = tag)
                }
            }
            Text(
                text = detail.title,
                style = MaterialTheme.typography.titleMedium.copy(fontSize = 20.sp),
                fontWeight = FontWeight.Bold,
                color = InsColors.TextPrimary,
            )
            Text(
                text = detail.timeText,
                style = MaterialTheme.typography.bodyLarge,
                color = InsColors.TextPrimary,
            )
            HorizontalDivider(modifier = Modifier.padding(vertical = AppSpacing.cardItemVerticalGap))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(AppSpacing.xxl),
            ) {
                DetailDescription(label = "예약 인원", value = "${detail.reservedCount}명", modifier = Modifier.weight(1f))
                DetailDescription(label = "정원", value = "${detail.capacity}명", modifier = Modifier.weight(1f))
            }
            DetailDescription(label = "설명", value = detail.description)
        }
    }
}

@Composable
private fun DetailDescription(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(AppSpacing.cardGap),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = InsColors.TextTertiary,
        )
        Text(
            text = value,
            style = MaterialTheme.typography.labelMedium,
            color = InsColors.TextPrimary,
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
                    status = ClassSessionStatus.SCHEDULED,
                    members = emptyList(),
                ),
        )
    }
}
