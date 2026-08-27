package com.classitda.feature.instructor.schedule.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.classitda.core.designsystem.AppShape
import com.classitda.core.designsystem.AppSpacing
import com.classitda.core.designsystem.InsColors

@Composable
internal fun InstructorCalendarFooter(
    onTodayClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(modifier = modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Row(horizontalArrangement = Arrangement.spacedBy(AppSpacing.lg)) {
            InstructorCalendarLegendItem("예정 수업", InsColors.Purple)
            InstructorCalendarLegendItem("완료 수업", InsColors.Gray400)
        }
        Spacer(Modifier.weight(1f))
        Box(
            modifier = Modifier.size(48.dp).clickable(onClick = onTodayClick),
            contentAlignment = Alignment.Center,
        ) {
            Box(
                modifier = Modifier.width(44.dp).height(28.dp).border(1.dp, InsColors.Divider, AppShape.Pill),
                contentAlignment = Alignment.Center,
            ) {
                Text("오늘", color = InsColors.TextSecondary, style = MaterialTheme.typography.labelSmall)
            }
        }
    }
}

@Composable
private fun InstructorCalendarLegendItem(
    text: String,
    color: Color,
) {
    Row(horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm), verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.width(4.dp).height(4.dp).background(color, AppShape.Pill))
        Text(text = text, color = InsColors.TextSecondary, style = MaterialTheme.typography.labelSmall)
    }
}
