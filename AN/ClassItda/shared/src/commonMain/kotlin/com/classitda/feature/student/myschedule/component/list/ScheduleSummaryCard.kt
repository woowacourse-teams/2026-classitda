package com.classitda.feature.student.myschedule.component.list

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import classitda.shared.generated.resources.Res
import classitda.shared.generated.resources.my_schedule_separator
import com.classitda.core.designsystem.AppShape
import com.classitda.core.designsystem.AppSpacing
import com.classitda.core.designsystem.appTypography
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun ScheduleSummaryCard(
    scheduleLabel: String,
    title: String,
    instructorName: String,
    memo: String?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    statusContent: @Composable () -> Unit,
) {
    val typography = appTypography()

    OutlinedCard(
        onClick = onClick,
        modifier =
            modifier
                .fillMaxWidth()
                .semantics(mergeDescendants = true) {},
        shape = AppShape.Card,
        colors = CardDefaults.outlinedCardColors(containerColor = MaterialTheme.colorScheme.surface),
    ) {
        Column(
            modifier = Modifier.padding(AppSpacing.cardPadding),
            verticalArrangement = Arrangement.spacedBy(AppSpacing.cardItemVerticalGap),
        ) {
            Text(
                text = scheduleLabel,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                style = typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(AppSpacing.cardItemHorizontalGap),
            ) {
                Text(
                    text = title,
                    modifier = Modifier.weight(1f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    style = typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface,
                )
                statusContent()
            }
            ScheduleSummaryCardDetails(
                instructorName = instructorName,
                memo = memo,
            )
        }
    }
}

@Composable
private fun ScheduleSummaryCardDetails(
    instructorName: String,
    memo: String?,
    modifier: Modifier = Modifier,
) {
    val typography = appTypography()

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(AppSpacing.cardItemHorizontalGap),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = instructorName,
            style = typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        memo?.let {
            Text(
                text = stringResource(Res.string.my_schedule_separator),
                style = typography.bodySmall,
                color = MaterialTheme.colorScheme.outlineVariant,
            )
            Text(
                text = it,
                modifier = Modifier.weight(1f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                style = typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
