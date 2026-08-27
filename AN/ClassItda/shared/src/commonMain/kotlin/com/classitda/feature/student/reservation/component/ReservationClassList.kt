package com.classitda.feature.student.reservation.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.classitda.core.designsystem.AppSpacing
import com.classitda.core.designsystem.AppTheme
import com.classitda.core.designsystem.StuColors
import com.classitda.feature.student.reservation.contract.ReservationClassCardType
import com.classitda.feature.student.reservation.contract.ReservationClassUiModel
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate

@Composable
internal fun ReservationClassList(
    year: Int,
    month: Int,
    selectedDayOfMonth: Int,
    classes: List<ReservationClassUiModel>,
    onClassButtonClick: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val selectedDate = LocalDate(year, month, selectedDayOfMonth)

    Column(
        modifier =
            modifier
                .fillMaxWidth()
                .background(StuColors.Background)
                .padding(
                    horizontal = AppSpacing.screenPadding,
                    vertical = AppSpacing.cardPadding,
                ),
    ) {
        Text(
            text = "${month}월 ${selectedDayOfMonth}일 ${selectedDate.dayOfWeek.koreanName}",
            color = StuColors.TextSecondary,
            style = MaterialTheme.typography.bodySmall,
        )
        Spacer(modifier = Modifier.height(AppSpacing.lg))

        classes.forEachIndexed { index, item ->
            ReservationTimelineItem(
                item = item,
                onButtonClick = { onClassButtonClick(item.id) },
            )
        }
    }
}

@Composable
private fun ReservationTimelineItem(
    item: ReservationClassUiModel,
    onButtonClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(modifier = modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.width(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Box(
                modifier =
                    Modifier
                        .padding(top = (1.5).dp)
                        .size(12.dp)
                        .border(2.dp, StuColors.PrimaryColor, CircleShape),
            )
            Box(
                modifier =
                    Modifier
                        .padding(top = AppSpacing.xs)
                        .width(1.dp)
                        .height(78.dp)
                        .background(StuColors.Divider),
            )
        }

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(AppSpacing.sm),
        ) {
            Text(
                text = item.classTime,
                color = StuColors.TextTertiary,
                style = MaterialTheme.typography.bodySmall,
            )

            when (item.cardType) {
                ReservationClassCardType.DEFAULT -> {
                    ClassCard(
                        className = item.className,
                        instructorName = item.instructorName,
                        memo = item.memo,
                        leftStudentCount = item.leftStudentCount,
                        onButtonClick = onButtonClick,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }

                ReservationClassCardType.RESERVED -> {
                    ReservationClassCard(
                        className = item.className,
                        instructorName = item.instructorName,
                        memo = item.memo,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }

                ReservationClassCardType.WAITLISTED -> {
                    WaitlistClassCard(
                        className = item.className,
                        instructorName = item.instructorName,
                        memo = item.memo,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }
        }
    }
}

private val DayOfWeek.koreanName: String
    get() =
        when (this) {
            DayOfWeek.MONDAY -> "월요일"
            DayOfWeek.TUESDAY -> "화요일"
            DayOfWeek.WEDNESDAY -> "수요일"
            DayOfWeek.THURSDAY -> "목요일"
            DayOfWeek.FRIDAY -> "금요일"
            DayOfWeek.SATURDAY -> "토요일"
            DayOfWeek.SUNDAY -> "일요일"
        }

@Preview(name = "예약 수업 목록", showBackground = true)
@Composable
private fun ReservationClassListPreview() {
    AppTheme {
        ReservationClassList(
            year = 2026,
            month = 8,
            selectedDayOfMonth = 8,
            classes = previewClasses,
            onClassButtonClick = {},
        )
    }
}

private val previewClasses =
    listOf(
        ReservationClassUiModel(
            id = "class-1",
            classTime = "오전 10:00 ~ 10:50",
            className = "리포머 베이직",
            instructorName = "이지은 강사",
            memo = "준비물 - 수건",
            leftStudentCount = 4,
            cardType = ReservationClassCardType.DEFAULT,
        ),
        ReservationClassUiModel(
            id = "class-2",
            classTime = "오후 2:00 ~ 2:50",
            className = "체어 밸런스",
            instructorName = "박소연 강사",
            memo = null,
            leftStudentCount = 0,
            cardType = ReservationClassCardType.RESERVED,
        ),
        ReservationClassUiModel(
            id = "class-3",
            classTime = "오후 7:30 ~ 8:20",
            className = "리포머 밸런스",
            instructorName = "이지은 강사",
            memo = "숙련자 대상",
            leftStudentCount = 0,
            cardType = ReservationClassCardType.WAITLISTED,
        ),
    )
