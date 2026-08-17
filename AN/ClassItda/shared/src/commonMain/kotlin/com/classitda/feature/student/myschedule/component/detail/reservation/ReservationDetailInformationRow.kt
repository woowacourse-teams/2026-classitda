package com.classitda.feature.student.myschedule.component.detail.reservation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import classitda.shared.generated.resources.Res
import classitda.shared.generated.resources.ic_calendar_today
import classitda.shared.generated.resources.my_schedule_class_detail_date
import com.classitda.core.designsystem.AppShape
import com.classitda.core.designsystem.AppSpacing
import com.classitda.core.designsystem.AppTheme
import com.classitda.core.designsystem.StuColors
import com.classitda.core.designsystem.ThemeType
import com.classitda.core.designsystem.appTypography
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun ReservationDetailInformationRow(
    icon: DrawableResource,
    label: String,
    content: @Composable () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(AppSpacing.lg),
        verticalAlignment = Alignment.Top,
    ) {
        Surface(
            shape = AppShape.Card,
            color = StuColors.Background,
        ) {
            Icon(
                painter = painterResource(icon),
                contentDescription = null,
                modifier =
                    Modifier
                        .padding(AppSpacing.md)
                        .size(AppSpacing.xxl),
                tint = StuColors.TextTertiary,
            )
        }
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(AppSpacing.xs),
        ) {
            Text(
                text = label,
                style = appTypography().bodySmall,
                color = StuColors.TextTertiary,
            )
            content()
        }
    }
}

@Preview(
    name = "Reservation detail information row / Student",
    group = "Component/MySchedule",
    showBackground = true,
    widthDp = 390,
)
@Composable
private fun ReservationDetailInformationRowPreview_Student() {
    AppTheme(theme = ThemeType.STUDENT) {
        Surface(color = StuColors.Surface) {
            ReservationDetailInformationRow(
                icon = Res.drawable.ic_calendar_today,
                label = stringResource(Res.string.my_schedule_class_detail_date),
            ) {
                Text(
                    text = "2026.08.04 (화)",
                    style = appTypography().bodyLarge.copy(fontWeight = FontWeight.Medium),
                    color = StuColors.TextPrimary,
                )
                Text(
                    text = "오후 6:30 ~ 7:20",
                    style = appTypography().bodyMedium,
                    color = StuColors.TextSecondary,
                )
            }
        }
    }
}
