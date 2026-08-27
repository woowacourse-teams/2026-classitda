package com.classitda.feature.instructor.classsession.edit.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.window.Dialog
import com.classitda.core.designsystem.AppShape
import com.classitda.core.designsystem.AppSpacing
import com.classitda.core.designsystem.AppTheme
import com.classitda.core.designsystem.InsColors
import com.classitda.core.designsystem.ThemeType
import kotlinx.datetime.LocalTime

private val AM_PM_OPTIONS = listOf("오전", "오후")
private val HOUR_OPTIONS = (1..12).toList()
private val MINUTE_OPTIONS = (0..59).toList()

@Composable
internal fun EditClassTimePickerDialog(
    initialTime: LocalTime,
    onDismissRequest: () -> Unit,
    onConfirm: (LocalTime) -> Unit,
) {
    var isPm by remember { mutableStateOf(initialTime.hour >= 12) }
    var hourIndex by remember { mutableStateOf(HOUR_OPTIONS.indexOf(initialTime.hour.toHour12())) }
    var minuteIndex by remember { mutableStateOf(initialTime.minute) }

    Dialog(onDismissRequest = onDismissRequest) {
        Surface(shape = AppShape.Card, color = InsColors.Surface) {
            Column(
                modifier = Modifier.padding(AppSpacing.lg),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(AppSpacing.md),
            ) {
                Text(
                    text = "시간 선택",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = InsColors.TextPrimary,
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm),
                ) {
                    EditWheelPicker(
                        items = AM_PM_OPTIONS,
                        selectedIndex = if (isPm) 1 else 0,
                        onSelectedIndexChanged = { isPm = it == 1 },
                        itemText = { it },
                        modifier = Modifier.weight(1f),
                    )
                    EditWheelPicker(
                        items = HOUR_OPTIONS,
                        selectedIndex = hourIndex,
                        onSelectedIndexChanged = { hourIndex = it },
                        itemText = { it.toString() },
                        modifier = Modifier.weight(1f),
                    )
                    EditWheelPicker(
                        items = MINUTE_OPTIONS,
                        selectedIndex = minuteIndex,
                        onSelectedIndexChanged = { minuteIndex = it },
                        itemText = { it.toString().padStart(2, '0') },
                        modifier = Modifier.weight(1f),
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                ) {
                    TextButton(onClick = onDismissRequest) {
                        Text(text = "취소", color = InsColors.TextSecondary)
                    }
                    TextButton(
                        onClick = {
                            val hour24 = HOUR_OPTIONS[hourIndex].toHour24(isPm)
                            onConfirm(LocalTime(hour24, minuteIndex))
                        },
                    ) {
                        Text(text = "확인", color = InsColors.Black)
                    }
                }
            }
        }
    }
}

private fun Int.toHour12(): Int = when {
    this == 0 -> 12
    this > 12 -> this - 12
    else -> this
}

private fun Int.toHour24(isPm: Boolean): Int = when {
    isPm && this != 12 -> this + 12
    !isPm && this == 12 -> 0
    else -> this
}

@Preview(name = "수정 화면 시간 선택", showBackground = true)
@Composable
private fun EditClassTimePickerDialogPreview() {
    AppTheme(theme = ThemeType.INSTRUCTOR) {
        EditClassTimePickerDialog(
            initialTime = LocalTime(10, 0),
            onDismissRequest = {},
            onConfirm = {},
        )
    }
}
