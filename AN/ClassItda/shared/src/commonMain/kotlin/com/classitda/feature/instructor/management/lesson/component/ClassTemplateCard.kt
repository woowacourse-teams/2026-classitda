package com.classitda.feature.instructor.management.lesson.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import classitda.shared.generated.resources.Res
import classitda.shared.generated.resources.ic_more
import com.classitda.core.designsystem.AppShape
import com.classitda.core.designsystem.AppSpacing
import com.classitda.core.designsystem.AppTheme
import com.classitda.core.designsystem.InsColors
import com.classitda.core.designsystem.ThemeType
import com.classitda.feature.instructor.management.lesson.model.ClassScheduleUiModel
import com.classitda.feature.instructor.management.lesson.model.ClassTemplateUiModel
import org.jetbrains.compose.resources.painterResource

@Composable
internal fun ClassTemplateCard(
    template: ClassTemplateUiModel,
    onClick: () -> Unit,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier =
            modifier
                .fillMaxWidth()
                .clickable(onClick = onClick)
                .clip(AppShape.Card)
                .background(InsColors.Surface)
                .padding(horizontal = AppSpacing.cardPadding)
                .padding(bottom = AppSpacing.cardPadding),
        verticalArrangement = Arrangement.spacedBy(AppSpacing.cardItemVerticalGap),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Row(
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.spacedBy(AppSpacing.xs),
            ) {
                template.tags.forEach { tag ->
                    Chip(
                        text = tag,
                    )
                }
            }

            var isMenuExpanded by remember { mutableStateOf(false) }

            Box {
                Icon(
                    painter = painterResource(Res.drawable.ic_more),
                    contentDescription = "더보기",
                    tint = InsColors.TextSecondary,
                    modifier =
                        Modifier
                            .size(48.dp)
                            .clickable { isMenuExpanded = true }
                            .padding(start = 24.dp),
                )

                DropdownMenu(
                    expanded = isMenuExpanded,
                    onDismissRequest = { isMenuExpanded = false },
                ) {
                    DropdownMenuItem(
                        text = { Text(text = "수정") },
                        onClick = {
                            isMenuExpanded = false
                            onEditClick()
                        },
                    )
                    DropdownMenuItem(
                        text = { Text(text = "삭제") },
                        onClick = {
                            isMenuExpanded = false
                            onDeleteClick()
                        },
                    )
                }
            }
        }

        Text(
            text = template.title,
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = InsColors.TextPrimary,
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            InfoRow(
                label = "진행 시간",
                value = template.durationText,
                modifier = Modifier.weight(1f),
            )
            InfoRow(
                label = "기본 정원",
                value = template.capacityText,
                modifier = Modifier.weight(1f),
            )
        }

        template.schedule?.let { schedule ->
            Row(modifier = Modifier.fillMaxWidth()) {
                InfoRow(
                    label = "수업 시간",
                    value = schedule.timeRangeText,
                    modifier = Modifier.weight(1f),
                )
                InfoRow(
                    label = "반복 요일",
                    value = schedule.repeatDaysText,
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

@Composable
private fun InfoRow(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = InsColors.TextSecondary,
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            color = InsColors.TextPrimary,
        )
    }
}

@Composable
@Preview
private fun ClassTemplateCardPreview_WithSchedule() {
    AppTheme(theme = ThemeType.INSTRUCTOR) {
        ClassTemplateCard(
            template =
                ClassTemplateUiModel(
                    id = "1",
                    tags = listOf("그룹 수업", "필라테스"),
                    title = "리포머 밸런스",
                    durationText = "50분",
                    capacityText = "8명",
                    schedule = ClassScheduleUiModel(timeRangeText = "10:00 ~ 10:50", repeatDaysText = "월, 수"),
                ),
            onClick = {},
            onEditClick = {},
            onDeleteClick = {},
        )
    }
}

@Composable
@Preview
private fun ClassTemplateCardPreview_WithoutSchedule() {
    AppTheme(theme = ThemeType.INSTRUCTOR) {
        ClassTemplateCard(
            template =
                ClassTemplateUiModel(
                    id = "2",
                    tags = listOf("개인 수업", "요가"),
                    title = "1:1 개인 수업",
                    durationText = "50분",
                    capacityText = "1명",
                    schedule = null,
                ),
            onClick = {},
            onEditClick = {},
            onDeleteClick = {},
        )
    }
}
