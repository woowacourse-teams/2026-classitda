package com.classitda.feature.student.reservation.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import classitda.shared.generated.resources.Res
import classitda.shared.generated.resources.ic_calendar_today
import classitda.shared.generated.resources.ic_check
import classitda.shared.generated.resources.ic_group
import classitda.shared.generated.resources.ic_schedule
import com.classitda.core.designsystem.AppShape
import com.classitda.core.designsystem.AppSpacing
import com.classitda.core.designsystem.AppTheme
import com.classitda.core.designsystem.StuColors
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource

// sealed interface 없이 우선 안에 ClassCard 형태랑 ReservationCard 형태로 분리
@Composable
internal fun ClassCard(
    className: String,
    instructorName: String,
    memo: String?,
    leftStudentCount: Int,
    onButtonClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.clickable(onClick = onButtonClick),
        shape = AppShape.Card,
        colors = CardDefaults.cardColors(
            containerColor = StuColors.White,
        ),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth().padding(horizontal = AppSpacing.cardPadding, vertical = AppSpacing.md),
            horizontalArrangement = Arrangement.spacedBy(AppSpacing.cardItemHorizontalGap),
        ) {
            ClassInfo(
                className = className,
                instructorName = instructorName,
                memo = memo,
                modifier = Modifier.weight(1f),
            )

            ReservationInfo(
                leftStudentCount = leftStudentCount,
            )
        }
    }
}

@Composable
internal fun ReservationClassCard(
    className: String,
    instructorName: String,
    memo: String?,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier,
        shape = AppShape.Card,
        colors = CardDefaults.cardColors(
            containerColor = StuColors.White,
        ),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = AppSpacing.cardPadding, vertical = AppSpacing.md),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(AppSpacing.cardItemHorizontalGap),
        ) {
            ClassInfo(
                className = className,
                instructorName = instructorName,
                memo = memo,
                modifier = Modifier.weight(1f),
            )

            ClassStatusLabel(
                icon = Res.drawable.ic_check,
                text = "예약 완료",
                color = StuColors.Green,
            )
        }
    }
}

@Composable
internal fun WaitlistClassCard(
    className: String,
    instructorName: String,
    memo: String?,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier,
        shape = AppShape.Card,
        colors = CardDefaults.cardColors(
            containerColor = StuColors.White,
        ),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = AppSpacing.cardPadding, vertical = AppSpacing.md),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(AppSpacing.cardItemHorizontalGap),
        ) {
            ClassInfo(
                className = className,
                instructorName = instructorName,
                memo = memo,
                modifier = Modifier.weight(1f),
            )

            WaitlistStatus()
        }
    }
}

@Composable
private fun ClassInfo(
    className: String,
    instructorName: String,
    memo: String?,
    modifier: Modifier = Modifier,
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(AppSpacing.xs),
        modifier = modifier,
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(AppSpacing.cardItemHorizontalGap),
        ) {
            Text(
                text = className,
                color = StuColors.TextPrimary,
                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(AppSpacing.md)) {
            Text(
                text = instructorName,
                color = StuColors.TextTertiary,
                style = MaterialTheme.typography.bodySmall,
            )
            memo?.let {
                Text(
                    text = it,
                    color = StuColors.TextTertiary,
                    style = MaterialTheme.typography.bodySmall,
                    overflow = TextOverflow.Ellipsis,
                    maxLines = 1,
                )
            }
        }
    }
}

@Composable
private fun ReservationInfo(
    leftStudentCount: Int,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(AppSpacing.xs),
    ) {
        ClassStatusLabel(
            icon = if (leftStudentCount > 0) {
                Res.drawable.ic_calendar_today
            } else {
                Res.drawable.ic_group
            },
            text = if (leftStudentCount > 0) "예약 가능" else "대기 가능",
            color = StuColors.TextPrimary,
            modifier = Modifier.align(Alignment.End),
        )
        Text(
            text = if (leftStudentCount > 0) "${leftStudentCount}자리 남음" else "정원 마감",
            color = StuColors.TextSecondary,
            style = MaterialTheme.typography.labelSmall,
            modifier = Modifier.align(Alignment.End),
        )
    }
}

@Composable
private fun WaitlistStatus(
    modifier: Modifier = Modifier,
) {
    ClassStatusLabel(
        icon = Res.drawable.ic_schedule,
        text = "대기 중",
        color = StuColors.Orange,
        modifier = modifier,
    )
}

@Composable
private fun ClassStatusLabel(
    icon: DrawableResource,
    text: String,
    color: Color,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(AppSpacing.xs),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            painter = painterResource(icon),
            contentDescription = null,
            modifier = Modifier.size(12.dp),
            tint = color,
        )
        Text(
            text = text,
            color = color,
            style = MaterialTheme.typography.labelSmall,
        )
    }
}

@Preview(name = "에약")
@Composable
private fun ClassCardPreview1() {
    AppTheme {
        ClassCard(
            className = "리포머 베이직",
            instructorName = "이지은 강사",
            memo = "정말정말정말정말정말정말정말정말정말정말정말정말정말정말정말정말정말정말 긴 메모의 글",
            leftStudentCount = 4,
            onButtonClick = {},
        )
    }
}

@Preview(name = "대기 에약")
@Composable
private fun ClassCardPreview2() {
    AppTheme {
        ClassCard(
            className = "리포머 베이직",
            instructorName = "이지은 강사",
            memo = "준비물 - 수건, 오늘 숙련자 대상이에요",
            leftStudentCount = 0,
            onButtonClick = {},
        )
    }
}

@Preview(name = "예약 완료")
@Composable
private fun ReservationClassCardPreview() {
    AppTheme {
        ReservationClassCard(
            className = "리포머 베이직",
            instructorName = "이지은 강사",
            memo = null,
        )
    }
}

@Preview(name = "대기 중")
@Composable
private fun WaitlistClassCardPreview() {
    AppTheme {
        WaitlistClassCard(
            className = "체어 베이직",
            instructorName = "박소연 강사",
            memo = "정말정말정말정말정말정말정말정말정말정말정말정말정말정말정말정말정말정말 긴 메모의 글",
        )
    }
}
