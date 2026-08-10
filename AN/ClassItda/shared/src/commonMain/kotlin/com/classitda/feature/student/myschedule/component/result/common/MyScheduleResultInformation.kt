package com.classitda.feature.student.myschedule.component.result.common

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import com.classitda.core.designsystem.AppSpacing
import com.classitda.core.designsystem.StuColors
import com.classitda.core.designsystem.appTypography

@Composable
internal fun MyScheduleResultSectionTitle(text: String) {
    Text(
        text = text,
        modifier = Modifier.semantics { heading() },
        style = appTypography().titleMedium.copy(fontWeight = FontWeight.Bold),
        color = StuColors.TextPrimary,
    )
}

@Composable
internal fun MyScheduleResultInformationRow(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    supportingValue: String? = null,
    valueColor: Color = StuColors.TextPrimary,
    supportingValueColor: Color = StuColors.TextSecondary,
) {
    val typography = appTypography()

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(AppSpacing.cardItemHorizontalGap),
        verticalAlignment = Alignment.Top,
    ) {
        Text(
            text = label,
            modifier = Modifier.weight(1f),
            style = typography.bodyMedium,
            color = StuColors.TextSecondary,
        )
        Column(
            modifier = Modifier.weight(2f),
            horizontalAlignment = Alignment.End,
            verticalArrangement = Arrangement.spacedBy(AppSpacing.cardItemVerticalGap),
        ) {
            Text(
                text = value,
                style = typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                color = valueColor,
                textAlign = TextAlign.End,
            )
            supportingValue?.let { supportingText ->
                Text(
                    text = supportingText,
                    style = typography.bodyMedium,
                    color = supportingValueColor,
                    textAlign = TextAlign.End,
                )
            }
        }
    }
}
