package com.classitda.feature.student.reservation

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.classitda.core.designsystem.AppSpacing
import com.classitda.core.designsystem.AppShape
import com.classitda.core.designsystem.AppTheme
import com.classitda.core.designsystem.StuColors
import com.classitda.feature.student.reservation.component.ClassCard
import com.classitda.feature.student.reservation.component.ReservationCalendar
import com.classitda.feature.student.reservation.component.ReservationClassCard
import com.classitda.core.designsystem.component.TopBar
import com.classitda.feature.student.reservation.component.WaitlistClassCard
import kotlinx.datetime.LocalDate

internal data class ReservationClassUiModel(
    val id: String,
    val classTime: String,
    val className: String,
    val instructorName: String,
    val roomName: String?,
    val leftStudentCount: Int,
    val cardType: ReservationClassCardType,
)

internal data class ReservationPassUiModel(
    val id: String,
    val name: String,
    val remainingText: String,
    val expirationText: String,
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
    passes: List<ReservationPassUiModel>,
    selectedPassId: String?,
    isPassSelectionVisible: Boolean,
    onPassClick: (String) -> Unit,
    onPassSelectionDismiss: () -> Unit,
    onPassSelectionClick: () -> Unit,
    onDayClick: (Int) -> Unit,
    onPreviousClick: () -> Unit,
    onNextClick: () -> Unit,
    onMonthModeChange: (Boolean) -> Unit,
    onTodayClick: () -> Unit,
    onClassButtonClick: (String) -> Unit,
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
                ReservationPassSelector(
                    passName = passes.firstOrNull { it.id == selectedPassId }?.name ?: "수강권 선택",
                    onClick = onPassSelectionClick,
                )
            }

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

    if (isPassSelectionVisible) {
        ReservationPassSelectionSheet(
            passes = passes,
            selectedPassId = selectedPassId,
            onPassClick = onPassClick,
            onDismissRequest = onPassSelectionDismiss,
        )
    }
}

@Composable
private fun ReservationPassSelector(
    passName: String,
    onClick: () -> Unit,
) {
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .background(StuColors.White)
                .clickable(onClick = onClick)
                .padding(horizontal = AppSpacing.screenPadding, vertical = AppSpacing.md),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = passName,
            color = StuColors.TextSecondary,
            style = MaterialTheme.typography.bodySmall,
        )
        Spacer(modifier = Modifier.weight(1f))
        Text(text = "⌄", color = StuColors.TextTertiary)
    }
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
private fun ReservationPassSelectionSheet(
    passes: List<ReservationPassUiModel>,
    selectedPassId: String?,
    onPassClick: (String) -> Unit,
    onDismissRequest: () -> Unit,
) {
    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        containerColor = StuColors.White,
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(
                start = AppSpacing.screenPadding,
                end = AppSpacing.screenPadding,
                bottom = AppSpacing.xxl,
            ),
            verticalArrangement = Arrangement.spacedBy(AppSpacing.sm),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "수강권 선택",
                    color = StuColors.TextPrimary,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                )
                Spacer(modifier = Modifier.weight(1f))
                Box(
                    modifier = Modifier.size(32.dp).clickable(onClick = onDismissRequest),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(text = "×", color = StuColors.TextSecondary)
                }
            }

            passes.forEach { pass ->
                val selected = pass.id == selectedPassId
                Card(
                    modifier = Modifier.fillMaxWidth().clickable { onPassClick(pass.id) },
                    shape = AppShape.Card,
                    border =
                        BorderStroke(
                            width = if (selected) 2.dp else 1.dp,
                            color = if (selected) StuColors.TextPrimary else StuColors.Divider,
                        ),
                    colors = CardDefaults.cardColors(containerColor = StuColors.White),
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(AppSpacing.cardPadding),
                        verticalArrangement = Arrangement.spacedBy(AppSpacing.xs),
                    ) {
                        Text(
                            text = pass.name,
                            color = StuColors.TextPrimary,
                            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                        )
                        Text(text = pass.remainingText, color = StuColors.TextSecondary, style = MaterialTheme.typography.bodySmall)
                        Text(text = pass.expirationText, color = StuColors.TextTertiary, style = MaterialTheme.typography.bodySmall)
                    }
                }
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
    onClassButtonClick: (String) -> Unit,
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
        id = "1",
        classTime = "오전 10:00 - 10:50",
        className = "리포머 베이직",
        instructorName = "이지은 강사",
        roomName = "리포머룸",
        leftStudentCount = 4,
        cardType = ReservationClassCardType.DEFAULT,
    ),

    ReservationClassUiModel(
        id = "2",
        classTime = "오후 2:00 - 2:50",
        className = "체어 밸런스",
        instructorName = "박소연 강사",
        roomName = "스튜디오 A",
        leftStudentCount = 0,
        cardType = ReservationClassCardType.DEFAULT,
    ),

    ReservationClassUiModel(
        id = "3",
        classTime = "오후 7:30 - 8:20",
        className = "리포머 밸런스",
        instructorName = "이지은 강사",
        roomName = "스튜디오 B",
        leftStudentCount = 0,
        cardType = ReservationClassCardType.RESERVED,
    ),

    ReservationClassUiModel(
        id = "4",
        classTime = "오후 9:30 - 10:20",
        className = "체어 베이직",
        instructorName = "박소연 강사",
        roomName = "바렐룸",
        leftStudentCount = 0,
        cardType = ReservationClassCardType.WAITLISTED,
    ),
)

private val previewPasses =
    listOf(
        ReservationPassUiModel("pass-1", "요가 10회권", "잔여 7회 / 예약 가능 7회", "2026.10.31까지"),
        ReservationPassUiModel("pass-2", "필라테스 20회권", "잔여 12회 / 예약 가능 12회", "2026.11.30까지"),
        ReservationPassUiModel("pass-3", "요가 / 필라테스 통합 1회권", "잔여 1회 / 예약 가능 1회", "2027.01.24까지"),
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
            passes = previewPasses,
            selectedPassId = "pass-1",
            isPassSelectionVisible = false,
            onPassClick = {},
            onPassSelectionDismiss = {},
            onPassSelectionClick = {},
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
            passes = previewPasses,
            selectedPassId = "pass-1",
            isPassSelectionVisible = false,
            onPassClick = {},
            onPassSelectionDismiss = {},
            onPassSelectionClick = {},
            onDayClick = {},
            onPreviousClick = {},
            onNextClick = {},
            onMonthModeChange = {},
            onTodayClick = {},
            onClassButtonClick = {},
        )
    }
}

@Preview(name = "수강권 선택 바텀시트")
@Composable
private fun ReservationPassSelectionSheetPreview() {
    AppTheme {
        ReservationPassSelectionSheet(
            passes = previewPasses,
            selectedPassId = "pass-1",
            onPassClick = {},
            onDismissRequest = {},
        )
    }
}
