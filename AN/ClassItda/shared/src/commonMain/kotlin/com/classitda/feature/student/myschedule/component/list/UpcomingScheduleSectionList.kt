package com.classitda.feature.student.myschedule.component.list

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp
import com.classitda.core.designsystem.AppSpacing
import com.classitda.core.designsystem.AppTheme
import com.classitda.core.designsystem.StuColors
import com.classitda.core.designsystem.ThemeType
import com.classitda.core.designsystem.appTypography
import com.classitda.domain.model.student.myschedule.ReservationId
import com.classitda.domain.model.student.myschedule.WaitlistId
import com.classitda.feature.student.myschedule.contract.UpcomingDateSectionUiModel
import com.classitda.feature.student.myschedule.contract.UpcomingScheduleCardUiModel
import com.classitda.feature.student.myschedule.preview.MyScheduleUpcomingPreviewFixture

@Composable
internal fun UpcomingScheduleSectionList(
    sections: List<UpcomingDateSectionUiModel>,
    onOpenReservation: (ReservationId) -> Unit,
    onOpenWaitlist: (WaitlistId) -> Unit,
    state: LazyListState = rememberLazyListState(),
    modifier: Modifier = Modifier,
) {
    val typography = appTypography()

    LazyColumn(
        modifier = modifier,
        state = state,
        contentPadding =
            PaddingValues(
                horizontal = AppSpacing.screenPadding,
                vertical = AppSpacing.sectionGap,
            ),
    ) {
        sections.forEachIndexed { sectionIndex, section ->
            item(key = "upcoming-date-${section.dateLabel}") {
                Text(
                    text = section.dateLabel,
                    modifier =
                        Modifier
                            .padding(bottom = AppSpacing.cardGap)
                            .semantics { heading() },
                    style =
                        typography.titleSmall.copy(
                            fontWeight = FontWeight.Bold,
                        ),
                    color = StuColors.TextPrimary,
                )
            }
            itemsIndexed(
                items = section.items,
                key = { _, item -> item.stableKey() },
            ) { itemIndex, item ->
                val isLastItemOfSection = itemIndex == section.items.lastIndex
                val isLastSection = sectionIndex == sections.lastIndex
                val itemModifier =
                    when {
                        isLastItemOfSection && !isLastSection -> {
                            Modifier.padding(bottom = AppSpacing.sectionGap)
                        }

                        !isLastItemOfSection -> {
                            Modifier.padding(bottom = AppSpacing.cardGap)
                        }

                        else -> {
                            Modifier
                        }
                    }

                UpcomingScheduleCard(
                    item = item,
                    onClick = {
                        when (item) {
                            is UpcomingScheduleCardUiModel.ConfirmedReservation -> {
                                onOpenReservation(item.reservationId)
                            }

                            is UpcomingScheduleCardUiModel.Waitlisted -> {
                                onOpenWaitlist(item.waitlistId)
                            }
                        }
                    },
                    modifier = itemModifier,
                )
            }
        }
    }
}

private fun UpcomingScheduleCardUiModel.stableKey(): String =
    when (this) {
        is UpcomingScheduleCardUiModel.ConfirmedReservation -> "reservation:${reservationId.value}"
        is UpcomingScheduleCardUiModel.Waitlisted -> "waitlist:${waitlistId.value}"
    }

@Preview(
    name = "Content · Student · Default",
    group = "Component/MySchedule/UpcomingList",
    showBackground = true,
    widthDp = 390,
    heightDp = 652,
)
@Composable
private fun UpcomingScheduleSectionListPreview_Content_Student_Default() {
    AppTheme(theme = ThemeType.STUDENT) {
        UpcomingScheduleSectionList(
            sections = MyScheduleUpcomingPreviewFixture.sections,
            onOpenReservation = {},
            onOpenWaitlist = {},
        )
    }
}
