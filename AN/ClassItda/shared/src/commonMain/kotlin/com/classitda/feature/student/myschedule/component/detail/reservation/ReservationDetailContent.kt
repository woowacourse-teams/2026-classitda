package com.classitda.feature.student.myschedule.component.detail.reservation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import classitda.shared.generated.resources.Res
import classitda.shared.generated.resources.my_schedule_class_detail_information
import classitda.shared.generated.resources.my_schedule_class_detail_instructor_information
import com.classitda.core.designsystem.AppSpacing
import com.classitda.core.designsystem.StuColors
import com.classitda.core.designsystem.appTypography
import com.classitda.feature.student.myschedule.contract.ReservationDetailUiModel
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun ReservationDetailContent(
    model: ReservationDetailUiModel,
    onCancelReservation: (() -> Unit)?,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier =
            modifier
                .fillMaxSize()
                .background(StuColors.Background),
    ) {
        item { ReservationDetailSummary(model = model) }
        item {
            ReservationDetailSectionHeader(
                title = stringResource(Res.string.my_schedule_class_detail_information),
            )
        }
        item { ReservationClassInfoSection(model = model) }
        item {
            ReservationDetailSectionHeader(
                title = stringResource(Res.string.my_schedule_class_detail_instructor_information),
            )
        }
        item { ReservationInstructorSection(model = model) }
        item {
            ReservationDetailFooter(
                model = model,
                onCancelReservation = onCancelReservation,
            )
        }
    }
}

@Composable
private fun ReservationDetailSectionHeader(title: String) {
    Text(
        text = title,
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(
                    horizontal = AppSpacing.screenPadding,
                    vertical = AppSpacing.lg,
                ).semantics { heading() },
        style = appTypography().titleMedium.copy(fontWeight = FontWeight.SemiBold),
        color = StuColors.TextSecondary,
    )
}
