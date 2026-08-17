package com.classitda.feature.student.myschedule.component.detail.reservation

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import classitda.shared.generated.resources.Res
import classitda.shared.generated.resources.my_schedule_instructor_avatar
import com.classitda.core.designsystem.AppSpacing
import com.classitda.core.designsystem.AppTheme
import com.classitda.core.designsystem.StuColors
import com.classitda.core.designsystem.ThemeType
import com.classitda.core.designsystem.appTypography
import com.classitda.feature.student.myschedule.contract.ReservationDetailUiModel
import com.classitda.feature.student.myschedule.preview.ReservationDetailPreviewFixture
import org.jetbrains.compose.resources.painterResource

@Composable
internal fun ReservationInstructorSection(model: ReservationDetailUiModel) {
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .background(StuColors.Surface)
                .padding(AppSpacing.screenPadding),
        horizontalArrangement = Arrangement.spacedBy(AppSpacing.lg),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Image(
            painter = painterResource(Res.drawable.my_schedule_instructor_avatar),
            contentDescription = null,
            modifier =
                Modifier
                    .size(AppSpacing.xxxl + AppSpacing.lg)
                    .clip(CircleShape),
        )
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(AppSpacing.xs),
        ) {
            Text(
                text = model.classInfo.instructorName,
                style = appTypography().bodyLarge.copy(fontWeight = FontWeight.Medium),
                color = StuColors.TextPrimary,
            )
            Text(
                text = model.classInfo.facilityName,
                style = appTypography().bodySmall,
                color = StuColors.TextSecondary,
            )
        }
    }
}

@Preview(
    name = "Reservation instructor / Student",
    group = "Component/MySchedule",
    showBackground = true,
    widthDp = 390,
)
@Composable
private fun ReservationInstructorSectionPreview_Student() {
    AppTheme(theme = ThemeType.STUDENT) {
        ReservationInstructorSection(model = ReservationDetailPreviewFixture.confirmed)
    }
}
