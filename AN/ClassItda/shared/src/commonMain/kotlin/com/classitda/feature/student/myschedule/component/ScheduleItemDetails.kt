package com.classitda.feature.student.myschedule.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import classitda.shared.generated.resources.Res
import classitda.shared.generated.resources.my_schedule_instructor_name
import com.classitda.core.designsystem.AppSpacing
import com.classitda.core.designsystem.AppTheme
import com.classitda.core.designsystem.ThemeType
import com.classitda.feature.student.myschedule.contract.MyScheduleItemUiModel
import com.classitda.feature.student.myschedule.preview.myScheduleReservationsPreviewItems
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun ScheduleItemDetails(
    item: MyScheduleItemUiModel,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(AppSpacing.cardItemVerticalGap),
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(AppSpacing.cardItemHorizontalGap),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = item.title,
                modifier = Modifier.weight(1f, fill = false),
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = stringResource(Res.string.my_schedule_instructor_name, item.instructorName),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        if (item.locationLabel.isNotBlank()) {
            Text(
                text = item.locationLabel,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Preview(
    name = "Details / Student",
    group = "Component/MySchedule",
    showBackground = true,
    widthDp = 390,
)
@Composable
private fun ScheduleItemDetailsPreview_Student_Default() {
    AppTheme(theme = ThemeType.STUDENT) {
        ScheduleItemDetails(
            item = myScheduleReservationsPreviewItems().first(),
            modifier = Modifier.padding(AppSpacing.screenPadding),
        )
    }
}
