package com.classitda.feature.instructor.classsession.edit.component

import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.window.DialogProperties
import com.classitda.core.designsystem.AppShape
import com.classitda.core.designsystem.AppTheme
import com.classitda.core.designsystem.InsColors
import com.classitda.core.designsystem.ThemeType
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Instant

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun ClassSessionDatePickerDialog(
    initialDate: LocalDate,
    onDismissRequest: () -> Unit,
    onConfirm: (LocalDate) -> Unit,
) {
    val datePickerState =
        rememberDatePickerState(
            initialSelectedDateMillis = initialDate.toPickerEpochMillis(),
        )

    DatePickerDialog(
        onDismissRequest = onDismissRequest,
        confirmButton = {
            TextButton(
                onClick = {
                    datePickerState.selectedDateMillis?.let { onConfirm(it.toPickerLocalDate()) }
                },
                enabled = datePickerState.selectedDateMillis != null,
            ) {
                Text("확인", color = InsColors.Purple)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismissRequest) {
                Text("취소", color = InsColors.TextSecondary)
            }
        },
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        ClassSessionDatePickerContent(state = datePickerState)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ClassSessionDatePickerContent(
    state: androidx.compose.material3.DatePickerState,
    modifier: Modifier = Modifier,
) {
    DatePicker(
        state = state,
        modifier = modifier,
        showModeToggle = false,
    )
}

private fun LocalDate.toPickerEpochMillis(): Long =
    LocalDateTime(this, LocalTime(0, 0))
        .toInstant(TimeZone.UTC)
        .toEpochMilliseconds()

private fun Long.toPickerLocalDate(): LocalDate =
    Instant
        .fromEpochMilliseconds(this)
        .toLocalDateTime(TimeZone.UTC)
        .date

@OptIn(ExperimentalMaterial3Api::class)
@Preview(name = "수업일 선택", showBackground = true, widthDp = 390)
@Composable
private fun ClassSessionDatePickerDialogPreview() {
    AppTheme(theme = ThemeType.INSTRUCTOR) {
        val state =
            rememberDatePickerState(
                initialSelectedDateMillis = LocalDate(2026, 8, 5).toPickerEpochMillis(),
            )
        androidx.compose.material3.Surface(
            shape = AppShape.Card,
            color = InsColors.White,
        ) {
            ClassSessionDatePickerContent(state = state)
        }
    }
}
