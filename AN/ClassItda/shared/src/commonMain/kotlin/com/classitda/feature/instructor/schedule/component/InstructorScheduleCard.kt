package com.classitda.feature.instructor.schedule.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.classitda.core.designsystem.AppShape
import com.classitda.core.designsystem.AppSpacing
import com.classitda.core.designsystem.InsColors
import com.classitda.domain.model.instructor.management.ClassSession
import com.classitda.feature.instructor.component.ClassSessionStatusBadge

@Composable
internal fun InstructorScheduleCard(
    session: ClassSession,
    modifier: Modifier = Modifier,
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = InsColors.White),
        modifier = modifier.padding(horizontal = AppSpacing.screenPadding, vertical = AppSpacing.xs).fillMaxWidth(),
    ) {
        Column(Modifier.padding(AppSpacing.cardPadding)) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Row(horizontalArrangement = Arrangement.spacedBy(AppSpacing.xs)) {
                    session.tags.take(2).forEach { tag ->
                        Surface(shape = AppShape.Pill, color = InsColors.SurfaceVariant) {
                            Text(
                                text = tag,
                                color = InsColors.TextSecondary,
                                style = MaterialTheme.typography.labelSmall,
                                modifier = Modifier.padding(horizontal = AppSpacing.sm, vertical = AppSpacing.xs),
                            )
                        }
                    }
                }
                Spacer(Modifier.weight(1f))
                ClassSessionStatusBadge(session.status)
            }
            Spacer(Modifier.height(AppSpacing.sm))
            Text(session.title, style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(AppSpacing.xs))
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = session.instructorTimeText(),
                    color = InsColors.TextSecondary,
                    style = MaterialTheme.typography.bodySmall,
                )
                Spacer(Modifier.weight(1f))
                Text(
                    text = "예약 ${session.reservedCount}명  |  정원 ${session.capacity}명",
                    color = InsColors.TextSecondary,
                    style = MaterialTheme.typography.bodySmall,
                )
            }
        }
    }
}

internal fun ClassSession.instructorTimeText(): String =
    "${startAt.hour.toString().padStart(2, '0')}:${startAt.minute.toString().padStart(2, '0')}"
