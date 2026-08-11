package com.classitda.feature.student.reservation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.classitda.core.designsystem.AppSpacing
import com.classitda.core.designsystem.AppTheme
import com.classitda.core.designsystem.StuColors
import com.classitda.feature.student.reservation.component.ClassCard
import com.classitda.feature.student.reservation.component.ReservationCalendar
import com.classitda.feature.student.reservation.component.ReservationClassCard
import com.classitda.core.designsystem.component.TopBar
import com.classitda.feature.student.reservation.component.WaitlistClassCard
import kotlinx.datetime.LocalDate

internal data class ReservationClassUiModel(
    val id: Long,
    val classTime: String,
    val className: String,
    val instructorName: String,
    val roomName: String?,
    val leftStudentCount: Int,
    val cardType: ReservationClassCardType,
)

internal enum class ReservationClassCardType {
    DEFAULT,
    RESERVED,
    WAITLISTED,
}

@Composable
internal fun ReservationScreen(
    year: Int,
    month: Int,
    selectedDayOfMonth: Int,
    todayDayOfMonth: Int?,
    confirmedReservationDays: Set<Int>,
    waitlistReservationDays: Set<Int>,
    isMonthMode: Boolean,
    classes: List<ReservationClassUiModel>,
    onDayClick: (Int) -> Unit,
    onPreviousClick: () -> Unit,
    onNextClick: () -> Unit,
    onMonthModeChange: (Boolean) -> Unit,
    onTodayClick: () -> Unit,
    onClassButtonClick: (Long) -> Unit,
    modifier: Modifier = Modifier,
    bottomBar: @Composable () -> Unit = {},
) {
    Scaffold(
        modifier = modifier,
        containerColor = StuColors.Background,
        topBar = {
            TopBar(
                title = "예약",
                hasBackground = true,
            )
        },
        bottomBar = bottomBar,
    ) { contentPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(contentPadding),
        ) {
            item {
                ReservationCalendar(
                    year = year,
                    month = month,
                    selectedDayOfMonth = selectedDayOfMonth,
                    todayDayOfMonth = todayDayOfMonth,
                    confirmedReservationDays = confirmedReservationDays,
                    waitlistReservationDays = waitlistReservationDays,
                    isMonthMode = isMonthMode,
                    onDayClick = onDayClick,
                    onPreviousClick = onPreviousClick,
                    onNextClick = onNextClick,
                    onMonthModeChange = onMonthModeChange,
                    onTodayClick = onTodayClick,
                )
            }

            item {
                ReservationClassList(
                    year = year,
                    month = month,
                    selectedDayOfMonth = selectedDayOfMonth,
                    classes = classes,
                    onClassButtonClick = onClassButtonClick,
                )
            }
        }
    }
}

@Composable
private fun ReservationClassList(
    year: Int,
    month: Int,
    selectedDayOfMonth: Int,
    classes: List<ReservationClassUiModel>,
    onClassButtonClick: (Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    val selectedDate = LocalDate(
        year = year,
        month = month,
        day = selectedDayOfMonth,
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(StuColors.Background)
            .padding(
                horizontal = AppSpacing.screenPadding,
                vertical = AppSpacing.cardPadding,
            ),
        verticalArrangement = Arrangement.spacedBy(AppSpacing.cardGap),
    ) {
        Text(
            text = "${month}월 ${selectedDayOfMonth}일 ${selectedDate.dayOfWeek.koreanName}",
            color = StuColors.TextSecondary,
            style = MaterialTheme.typography.titleMedium,
        )

        classes.forEach { item ->
            ReservationClassItem(
                item = item,
                onButtonClick = {
                    onClassButtonClick(item.id)
                },
            )
        }
    }
}

@Composable
private fun ReservationClassItem(
    item: ReservationClassUiModel,
    onButtonClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    when (item.cardType) {
        ReservationClassCardType.DEFAULT -> {
            ClassCard(
                classTime = item.classTime,
                className = item.className,
                instructorName = item.instructorName,
                roomName = item.roomName,
                leftStudentCount = item.leftStudentCount,
                onButtonClick = onButtonClick,
                modifier = modifier.fillMaxWidth(),
            )
        }

        ReservationClassCardType.RESERVED -> {
            ReservationClassCard(
                classTime = item.classTime,
                className = item.className,
                instructorName = item.instructorName,
                roomName = item.roomName,
                modifier = modifier.fillMaxWidth(),
            )
        }

        ReservationClassCardType.WAITLISTED -> {
            WaitlistClassCard(
                classTime = item.classTime,
                className = item.className,
                instructorName = item.instructorName,
                roomName = item.roomName,
                modifier = modifier.fillMaxWidth(),
            )
        }
    }
}

private val kotlinx.datetime.DayOfWeek.koreanName: String
    get() = when (this) {
        kotlinx.datetime.DayOfWeek.MONDAY -> "월요일"
        kotlinx.datetime.DayOfWeek.TUESDAY -> "화요일"
        kotlinx.datetime.DayOfWeek.WEDNESDAY -> "수요일"
        kotlinx.datetime.DayOfWeek.THURSDAY -> "목요일"
        kotlinx.datetime.DayOfWeek.FRIDAY -> "금요일"
        kotlinx.datetime.DayOfWeek.SATURDAY -> "토요일"
        kotlinx.datetime.DayOfWeek.SUNDAY -> "일요일"
    }

private val previewClasses = listOf(
    ReservationClassUiModel(
        id = 1L,
        classTime = "오전 10:00 - 10:50",
        className = "리포머 베이직",
        instructorName = "이지은 강사",
        roomName = "리포머룸",
        leftStudentCount = 4,
        cardType = ReservationClassCardType.DEFAULT,
    ),

    ReservationClassUiModel(
        id = 2L,
        classTime = "오후 2:00 - 2:50",
        className = "체어 밸런스",
        instructorName = "박소연 강사",
        roomName = "스튜디오 A",
        leftStudentCount = 0,
        cardType = ReservationClassCardType.DEFAULT,
    ),

    ReservationClassUiModel(
        id = 3L,
        classTime = "오후 7:30 - 8:20",
        className = "리포머 밸런스",
        instructorName = "이지은 강사",
        roomName = "스튜디오 B",
        leftStudentCount = 0,
        cardType = ReservationClassCardType.RESERVED,
    ),

    ReservationClassUiModel(
        id = 4L,
        classTime = "오후 9:30 - 10:20",
        className = "체어 베이직",
        instructorName = "박소연 강사",
        roomName = "바렐룸",
        leftStudentCount = 0,
        cardType = ReservationClassCardType.WAITLISTED,
    ),
)

@Preview(name = "주간 예약 화면")
@Composable
private fun ReservationScreenWeekPreview() {
    AppTheme {
        ReservationScreen(
            year = 2026,
            month = 8,
            selectedDayOfMonth = 8,
            todayDayOfMonth = 5,
            confirmedReservationDays = setOf(7, 8),
            waitlistReservationDays = setOf(9),
            isMonthMode = false,
            classes = previewClasses,
            onDayClick = {},
            onPreviousClick = {},
            onNextClick = {},
            onMonthModeChange = {},
            onTodayClick = {},
            onClassButtonClick = {},
        )
    }
}

@Preview(name = "월간 예약 화면")
@Composable
private fun ReservationScreenMonthPreview() {
    AppTheme {
        ReservationScreen(
            year = 2026,
            month = 8,
            selectedDayOfMonth = 8,
            todayDayOfMonth = 5,
            confirmedReservationDays = setOf(7, 8, 12, 15),
            waitlistReservationDays = setOf(9, 12),
            isMonthMode = true,
            classes = previewClasses,
            onDayClick = {},
            onPreviousClick = {},
            onNextClick = {},
            onMonthModeChange = {},
            onTodayClick = {},
            onClassButtonClick = {},
        )
    }
}
