package com.classitda.feature.instructor.classsession.edit.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.classitda.core.designsystem.AppShape
import com.classitda.core.designsystem.AppSpacing
import com.classitda.core.designsystem.AppTheme
import com.classitda.core.designsystem.InsColors
import com.classitda.core.designsystem.ThemeType

@Composable
internal fun EditClassStartTimeField(
    label: String,
    startTimeText: String,
    endTimeText: String,
    onStartTimeClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(EditFieldDefaults.labelFieldGap),
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
            color = InsColors.TextPrimary,
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm),
        ) {
            EditTimeBox(
                text = startTimeText,
                suffixText = "부터",
                modifier = Modifier.weight(1f).clickable(onClick = onStartTimeClick),
                containerColor = InsColors.Surface,
                borderColor = InsColors.Divider,
                textColor = InsColors.TextPrimary,
            )
            EditTimeBox(
                text = endTimeText,
                suffixText = "까지",
                modifier = Modifier.weight(1f),
                containerColor = InsColors.SurfaceVariant,
                borderColor = InsColors.SurfaceVariant,
                textColor = InsColors.TextSecondary,
            )
        }
    }
}

@Composable
private fun EditTimeBox(
    text: String,
    suffixText: String,
    containerColor: Color,
    borderColor: Color,
    textColor: Color,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(
            modifier =
                Modifier
                    .weight(1f)
                    .height(EditFieldDefaults.height)
                    .clip(AppShape.Card)
                    .background(containerColor, AppShape.Card)
                    .border(1.dp, borderColor, AppShape.Card)
                    .padding(horizontal = AppSpacing.md),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = text,
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.bodySmall,
                color = textColor,
            )
        }
        Text(
            text = suffixText,
            modifier = Modifier.padding(start = AppSpacing.sm),
            style = MaterialTheme.typography.labelSmall,
            color = InsColors.TextSecondary,
        )
    }
}

@Preview(name = "수정 화면 수업 시간", showBackground = true)
@Composable
private fun EditClassStartTimeFieldPreview() {
    AppTheme(theme = ThemeType.INSTRUCTOR) {
        EditClassStartTimeField(
            label = "수업 시간 *",
            startTimeText = "오전 10:00",
            endTimeText = "오전 10:50",
            onStartTimeClick = {},
        )
    }
}
