package com.classitda.feature.student.reservation.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.classitda.core.designsystem.AppShape
import com.classitda.core.designsystem.AppSpacing
import com.classitda.core.designsystem.AppTheme
import com.classitda.core.designsystem.StuColors

// sealed interface 없이 우선 안에 ClassCard 형태랑 ReservationCard 형태로 분리
@Composable
internal fun ClassCard(
    classTime: String,
    className: String,
    instructorName: String,
    roomName: String?,
    leftStudentCount: Int,
    onButtonClick: () -> Unit,
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
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(AppSpacing.cardPadding),
        ) {
            ClassInfo(
                classTime = classTime,
                className = className,
                instructorName = instructorName,
                roomName = roomName,
            )

            Spacer(modifier = Modifier.weight(1f))

            ReservationInfo(
                leftStudentCount = leftStudentCount,
                onButtonClick = onButtonClick,
            )
        }
    }
}

@Composable
internal fun ReservationClassCard(
    classTime: String,
    className: String,
    instructorName: String,
    roomName: String?,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier,
        shape = AppShape.Card,
        colors = CardDefaults.cardColors(
            containerColor = StuColors.SecondaryGreen,
        ),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(AppSpacing.cardPadding),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            ClassInfo(
                classTime = classTime,
                className = className,
                instructorName = instructorName,
                roomName = roomName,
                modifier = Modifier.weight(1f),
            )

            Text(
                text = "✓ 예약 완료",
                color = StuColors.PrimaryGreen,
                style = MaterialTheme.typography.labelMedium,
            )
        }
    }
}

@Composable
internal fun WaitlistClassCard(
    classTime: String,
    className: String,
    instructorName: String,
    roomName: String?,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier,
        shape = AppShape.Card,
        colors = CardDefaults.cardColors(
            containerColor = StuColors.SecondaryOrange,
        ),
        border = BorderStroke(
            width = 1.dp,
            color = StuColors.AccentOrange,
        ),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(AppSpacing.cardPadding),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            ClassInfo(
                classTime = classTime,
                className = className,
                instructorName = instructorName,
                roomName = roomName,
                modifier = Modifier.weight(1f),
            )

            WaitlistStatus()
        }
    }
}

@Composable
private fun ClassInfo(
    classTime: String,
    className: String,
    instructorName: String,
    roomName: String?,
    modifier: Modifier = Modifier,
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(AppSpacing.cardItemVerticalGap),
        modifier = modifier,
    ) {
        Text(
            text = classTime,
            color = StuColors.TextSecondary,
            style = MaterialTheme.typography.bodyMedium,
        )
        Row(
            horizontalArrangement = Arrangement.spacedBy(AppSpacing.cardItemHorizontalGap),
        ) {
            Text(
                text = className,
                color = StuColors.TextPrimary,
                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
            )
            Text(
                text = instructorName,
                color = StuColors.TextSecondary,
                style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp),
                modifier = Modifier.align(Alignment.Bottom),
            )
        }
        if (roomName != null) Text(
            text = roomName,
            color = StuColors.TextSecondary,
            style = MaterialTheme.typography.bodyMedium,
        )
    }
}

@Composable
private fun ReservationInfo(
    leftStudentCount: Int,
    onButtonClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(AppSpacing.cardItemVerticalGap),
    ) {
        if (leftStudentCount != 0) Text(
            text = leftStudentCount.toString() + "자리 남음",
            color = StuColors.PrimaryGreen,
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.align(Alignment.End),
        ) else Text(
            text = "마감",
            color = StuColors.TextSecondary,
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.align(Alignment.End),
        )

        if (leftStudentCount != 0) ReserveButton(onClick = onButtonClick)
        else WaitlistButton(onClick = onButtonClick)
    }
}

@Composable
private fun WaitlistStatus(
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .border(
                width = 1.dp,
                color = StuColors.AccentOrange,
                shape = AppShape.Pill,
            )
            .padding(
                horizontal = AppSpacing.chipHorizontalPadding,
                vertical = AppSpacing.chipVerticalPadding,
            ),
        horizontalArrangement = Arrangement.spacedBy(AppSpacing.chipIconGap),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(4.dp)
                .background(
                    color = StuColors.AccentOrange,
                    shape = CircleShape,
                ),
        )

        Text(
            text = "대기 중",
            color = StuColors.DarkOrange,
            style = MaterialTheme.typography.labelSmall,
        )
    }
}

@Preview(name = "에약")
@Composable
private fun ClassCardPreview1() {
    AppTheme {
        ClassCard(
            classTime = "오전 10:00 - 10:50",
            className = "리포머 베이직",
            instructorName = "이지은 강사",
            roomName = "리포머룸",
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
            classTime = "오전 10:00 - 10:50",
            className = "리포머 베이직",
            instructorName = "이지은 강사",
            roomName = "리포머룸",
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
            classTime = "오전 10:00 - 10:50",
            className = "리포머 베이직",
            instructorName = "이지은 강사",
            roomName = "리포머룸",
        )
    }
}

@Preview(name = "대기 중")
@Composable
private fun WaitlistClassCardPreview() {
    AppTheme {
        WaitlistClassCard(
            classTime = "오후 9:30 - 10:20",
            className = "체어 베이직",
            instructorName = "박소연 강사",
            roomName = "바렐룸",
        )
    }
}
