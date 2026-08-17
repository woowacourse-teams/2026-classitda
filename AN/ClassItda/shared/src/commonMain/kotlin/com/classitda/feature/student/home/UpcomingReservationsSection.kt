package com.classitda.feature.student.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import classitda.shared.generated.resources.Res
import classitda.shared.generated.resources.ic_arrow_forward
import classitda.shared.generated.resources.ic_clock
import classitda.shared.generated.resources.ic_memo
import classitda.shared.generated.resources.ic_person
import com.classitda.core.designsystem.AppShape
import com.classitda.core.designsystem.AppSpacing
import com.classitda.core.designsystem.AppTheme
import com.classitda.core.designsystem.StuColors
import com.classitda.feature.student.home.component.PrimaryTextButton
import org.jetbrains.compose.resources.painterResource

@Composable
fun UpcomingReservationsSection(
    classDateTime: String,
    className: String,
    instructorName: String,
    memo: String,
    remainingTime: String,
    onDetailClick: () -> Unit,
    onSeeAllClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "다가오는 일정",
                style =
                    MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.SemiBold,
                    ),
                color = StuColors.TextSecondary,
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(vertical = AppSpacing.xs).clickable(onClick = onSeeAllClick),
            ) {
                Text(
                    text = "전체 보기",
                    style = MaterialTheme.typography.labelLarge.copy(fontSize = 13.sp),
                    color = StuColors.TextSecondary,
                )
                Icon(
                    painter = painterResource(Res.drawable.ic_arrow_forward),
                    contentDescription = null,
                    tint = StuColors.TextSecondary,
                    modifier = Modifier.padding(start = AppSpacing.xs).size(12.dp),
                )
            }
        }

        Spacer(Modifier.height(AppSpacing.sm))

        UpComingCard(
            classDateTime = classDateTime,
            className = className,
            instructorName = instructorName,
            memo = memo,
            remainingTime = remainingTime,
            onDetailClick = onDetailClick,
        )
    }
}

@Composable
fun UpComingCard(
    classDateTime: String,
    className: String,
    instructorName: String,
    memo: String,
    remainingTime: String,
    onDetailClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier =
            modifier
                .fillMaxWidth()
                .background(StuColors.Surface, AppShape.Card)
                .padding(AppSpacing.cardPadding),
    ) {
        Text(
            text = classDateTime,
            modifier = Modifier.align(Alignment.End),
            style = MaterialTheme.typography.labelLarge,
            color = StuColors.PrimaryBlack,
        )

        Text(
            text = className,
            style =
                MaterialTheme.typography.headlineSmall.copy(
                    fontSize = 22.sp,
                    lineHeight = 28.sp,
                    fontWeight = FontWeight.Bold,
                ),
            color = StuColors.TextPrimary,
        )

        Spacer(Modifier.height(AppSpacing.md))

        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                painter = painterResource(Res.drawable.ic_person),
                contentDescription = "instructorIcon",
                tint = StuColors.TextSecondary,
                modifier = Modifier.size(16.dp),
            )
            Text(
                text = "$instructorName 강사",
                style = MaterialTheme.typography.bodyMedium,
                color = StuColors.TextSecondary,
                modifier = Modifier.padding(start = AppSpacing.xs),
            )
        }

        Spacer(Modifier.height(AppSpacing.sm))

        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                painter = painterResource(Res.drawable.ic_memo),
                contentDescription = "locationIcon",
                tint = StuColors.TextSecondary,
                modifier = Modifier.size(16.dp),
            )
            Text(
                text = memo,
                style = MaterialTheme.typography.bodyMedium,
                color = StuColors.TextSecondary,
                modifier = Modifier.padding(start = AppSpacing.xs),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }

        HorizontalDivider(
            modifier = Modifier.padding(vertical = AppSpacing.md),
            color = StuColors.Divider,
            thickness = 1.dp,
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row {
                Icon(
                    painter = painterResource(Res.drawable.ic_clock),
                    contentDescription = "clockIcon",
                    tint = StuColors.PrimaryBlack,
                    modifier = Modifier.size(16.dp),
                )
                Text(
                    text = remainingTime,
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = StuColors.PrimaryBlack,
                    modifier = Modifier.padding(start = AppSpacing.xs),
                )
            }
            PrimaryTextButton(
                "예약 상세",
                onClick = onDetailClick,
            )
        }
    }
}

@Composable
@Preview(showBackground = true, backgroundColor = 0xffF4F6F5)
private fun UpcomingReservationsSectionPreview() {
    AppTheme {
        UpcomingReservationsSection(
            classDateTime = "8월 7일(금) 오후 7:30",
            className = "리포머 밸런스",
            instructorName = "이지은",
            memo = "준비물ㅇㅁㄴㅇㄹㅁㄴㅇㄹㄴㅇㄹㄴㅁㅇㄹㅁㄴㅇㄹㅁㄴㅇㄹㅁㄴㅇㄹㅁㄴㅇ란모ㅓㅇㄴ마엃",
            remainingTime = "22시간 뒤",
            onDetailClick = {},
            onSeeAllClick = {},
        )
    }
}
