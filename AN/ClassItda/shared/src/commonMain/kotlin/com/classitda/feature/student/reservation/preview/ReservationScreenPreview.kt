package com.classitda.feature.student.reservation.preview

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.classitda.feature.student.reservation.ReservationScreenContent
import com.classitda.feature.student.reservation.contract.ReservationClassCardType
import com.classitda.feature.student.reservation.contract.ReservationClassUiModel
import com.classitda.feature.student.reservation.contract.ReservationPassUiModel
import kotlinx.datetime.LocalDate

private val previewClasses =
    listOf(
        ReservationClassUiModel(
            id = "1",
            classTime = "오전 10:00 - 10:50",
            className = "리포머 베이직",
            instructorName = "이지은 강사",
            memo = "정말정말정말정말정말정말정말정말정말 긴 메모의 글",
            leftStudentCount = 4,
            cardType = ReservationClassCardType.DEFAULT,
        ),
        ReservationClassUiModel(
            id = "2",
            classTime = "오후 2:00 - 2:50",
            className = "체어 밸런스",
            instructorName = "박소연 강사",
            memo = "준비물 - 수건, 오늘 숙련자 대상이에요",
            leftStudentCount = 0,
            cardType = ReservationClassCardType.DEFAULT,
        ),
        ReservationClassUiModel(
            id = "3",
            classTime = "오후 7:30 - 8:20",
            className = "리포머 밸런스",
            instructorName = "이지은 강사",
            memo = "준비물 - 수건, 오늘 숙련자 대상이에요",
            leftStudentCount = 0,
            cardType = ReservationClassCardType.RESERVED,
        ),
        ReservationClassUiModel(
            id = "4",
            classTime = "오후 9:30 - 10:20",
            className = "체어 베이직",
            instructorName = "박소연 강사",
            memo = null,
            leftStudentCount = 0,
            cardType = ReservationClassCardType.WAITLISTED,
        ),
    )

private val previewClassesByDay =
    mapOf(
        7 to previewClasses.filter { it.cardType == ReservationClassCardType.RESERVED },
        8 to previewClasses.filter { it.cardType == ReservationClassCardType.DEFAULT },
        9 to previewClasses.filter { it.cardType == ReservationClassCardType.WAITLISTED },
    )

private val previewPasses =
    listOf(
        ReservationPassUiModel("pass-1", "요가 10회권", "잔여 7회 / 예약 가능 7회", "유효기간: 2026.08.01 ~ 2026.10.31"),
        ReservationPassUiModel("pass-2", "필라테스 20회권", "잔여 12회 / 예약 가능 12회", "유효기간: 2026.08.01 ~ 2026.11.30"),
        ReservationPassUiModel("pass-3", "요가 / 필라테스 통합 1회권", "잔여 1회 / 예약 가능 1회", "유효기간: 2026.08.01 ~ 2027.01.24"),
    )

@Composable
internal fun ReservationScreenPreview(initialMonthMode: Boolean) {
    var year by remember { mutableStateOf(2026) }
    var month by remember { mutableStateOf(8) }
    var selectedDayOfMonth by remember { mutableStateOf(8) }
    var selectedPassId by remember { mutableStateOf<String?>("pass-1") }
    var isPassSelectionVisible by remember { mutableStateOf(false) }
    var isMonthMode by remember { mutableStateOf(initialMonthMode) }
    var selectedClassId by remember { mutableStateOf<String?>(null) }
    val isPreviewMonth = year == 2026 && month == 8

    ReservationScreenContent(
        year = year,
        month = month,
        selectedDayOfMonth = selectedDayOfMonth,
        today = LocalDate(2026, 8, 5),
        confirmedReservationDays = if (isPreviewMonth) setOf(7) else emptySet(),
        waitlistReservationDays = if (isPreviewMonth) setOf(9) else emptySet(),
        isMonthMode = isMonthMode,
        classes = if (isPreviewMonth) previewClassesByDay[selectedDayOfMonth].orEmpty() else emptyList(),
        passes = previewPasses,
        selectedPassId = selectedPassId,
        isPassSelectionVisible = isPassSelectionVisible,
        onPassClick = {
            selectedPassId = it
            isPassSelectionVisible = false
        },
        onPassSelectionDismiss = { isPassSelectionVisible = false },
        onPassSelectionClick = { isPassSelectionVisible = true },
        onDayClick = { selectedDayOfMonth = it },
        onPreviousClick = {
            if (month == 1) {
                year -= 1
                month = 12
            } else {
                month -= 1
            }
            selectedDayOfMonth = 1
        },
        onNextClick = {
            if (month == 12) {
                year += 1
                month = 1
            } else {
                month += 1
            }
            selectedDayOfMonth = 1
        },
        onMonthModeChange = { isMonthMode = it },
        onTodayClick = {
            year = 2026
            month = 8
            selectedDayOfMonth = 5
        },
        onClassButtonClick = { selectedClassId = it },
    )

    selectedClassId
        ?.let { classId -> previewClasses.firstOrNull { it.id == classId } }
        ?.let { selectedClass ->
            AlertDialog(
                onDismissRequest = { selectedClassId = null },
                title = { Text(text = "수업 선택") },
                text = { Text(text = "${selectedClass.className} 수업을 선택했어요.") },
                confirmButton = {
                    TextButton(onClick = { selectedClassId = null }) {
                        Text(text = "확인")
                    }
                },
            )
        }
}
