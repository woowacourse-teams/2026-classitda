package com.classitda.feature.student.myschedule.component.result.reservation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import classitda.shared.generated.resources.Res
import classitda.shared.generated.resources.my_schedule_book_another_class
import classitda.shared.generated.resources.my_schedule_return_to_list
import com.classitda.core.designsystem.AppSpacing
import com.classitda.core.designsystem.AppTheme
import com.classitda.core.designsystem.StuColors
import com.classitda.core.designsystem.ThemeType
import com.classitda.feature.student.myschedule.component.common.MySchedulePrimaryButton
import com.classitda.feature.student.myschedule.component.common.MyScheduleSecondaryButton
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun ReservationCancelledActionSection(
    onBookAnotherClass: () -> Unit,
    onReturnToList: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier =
            modifier
                .fillMaxWidth()
                .background(StuColors.Background)
                .padding(
                    horizontal = AppSpacing.screenPadding,
                    vertical = AppSpacing.lg,
                ),
        verticalArrangement = Arrangement.spacedBy(AppSpacing.cardGap),
    ) {
        MySchedulePrimaryButton(
            text = stringResource(Res.string.my_schedule_book_another_class),
            onClick = onBookAnotherClass,
        )
        MyScheduleSecondaryButton(
            text = stringResource(Res.string.my_schedule_return_to_list),
            onClick = onReturnToList,
        )
    }
}

@Preview(
    name = "Reservation cancelled actions · Student · Default",
    group = "Component/MySchedule",
    showBackground = true,
    widthDp = 390,
)
@Composable
private fun ReservationCancelledActionSectionPreview_Default_Student() {
    AppTheme(theme = ThemeType.STUDENT) {
        ReservationCancelledActionSection(
            onBookAnotherClass = {},
            onReturnToList = {},
        )
    }
}
