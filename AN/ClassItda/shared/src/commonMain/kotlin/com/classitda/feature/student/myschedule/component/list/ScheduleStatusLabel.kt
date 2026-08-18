package com.classitda.feature.student.myschedule.component.list

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import com.classitda.core.designsystem.AppSpacing
import com.classitda.core.designsystem.appTypography
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun ScheduleStatusLabel(
    label: StringResource,
    contentColor: Color,
    modifier: Modifier = Modifier,
    mark: StringResource? = null,
) {
    val typography = appTypography()

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(AppSpacing.cardItemHorizontalGap),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        mark?.let {
            Text(
                text = stringResource(it),
                style = typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                color = contentColor,
            )
        }
        Text(
            text = stringResource(label),
            style = typography.bodySmall.copy(fontWeight = FontWeight.Bold),
            color = contentColor,
        )
    }
}
