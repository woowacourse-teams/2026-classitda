package com.classitda.feature.student.myschedule.component.detail.history

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import classitda.shared.generated.resources.Res
import classitda.shared.generated.resources.ic_calendar_today
import classitda.shared.generated.resources.ic_confirmation_number
import classitda.shared.generated.resources.ic_location_on
import classitda.shared.generated.resources.my_schedule_bullet
import classitda.shared.generated.resources.my_schedule_class_detail_date
import classitda.shared.generated.resources.my_schedule_class_detail_history_condition
import classitda.shared.generated.resources.my_schedule_class_detail_information
import classitda.shared.generated.resources.my_schedule_class_detail_instructor
import classitda.shared.generated.resources.my_schedule_class_detail_location
import classitda.shared.generated.resources.my_schedule_class_detail_notice
import classitda.shared.generated.resources.my_schedule_class_detail_open_instructor
import classitda.shared.generated.resources.my_schedule_class_detail_ticket
import classitda.shared.generated.resources.my_schedule_class_detail_ticket_counts
import classitda.shared.generated.resources.my_schedule_class_detail_ticket_inquiry
import classitda.shared.generated.resources.my_schedule_class_detail_time_with_duration
import classitda.shared.generated.resources.my_schedule_inquiry
import classitda.shared.generated.resources.my_schedule_instructor_avatar
import classitda.shared.generated.resources.my_schedule_instructor_name
import classitda.shared.generated.resources.my_schedule_open_detail_mark
import com.classitda.core.designsystem.AppShape
import com.classitda.core.designsystem.AppSpacing
import com.classitda.core.designsystem.AppTheme
import com.classitda.core.designsystem.StuColors
import com.classitda.core.designsystem.ThemeType
import com.classitda.core.designsystem.appTypography
import com.classitda.feature.student.myschedule.component.common.MySchedulePrimaryButton
import com.classitda.feature.student.myschedule.component.common.ScheduleStatusChip
import com.classitda.feature.student.myschedule.component.common.ScheduleStatusChipType
import com.classitda.feature.student.myschedule.contract.CompletedClassDetailUiModel
import com.classitda.feature.student.myschedule.preview.completedClassDetailPreviewModel
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun CompletedClassDetailContent(
    model: CompletedClassDetailUiModel,
    onOpenInstructor: () -> Unit,
    onInquiry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier =
            modifier
                .fillMaxSize()
                .background(StuColors.Background),
    ) {
        item { CompletedClassSummarySection(model = model) }
        item { SectionGap() }
        item { CompletedClassInformationSection(model = model) }
        item { SectionGap() }
        item {
            CompletedClassInstructorSection(
                model = model,
                onClick = onOpenInstructor,
            )
        }
        item { SectionGap() }
        item { CompletedClassNoticeSection() }
        item {
            MySchedulePrimaryButton(
                text = stringResource(Res.string.my_schedule_inquiry),
                onClick = onInquiry,
                modifier =
                    Modifier
                        .padding(horizontal = AppSpacing.screenPadding)
                        .padding(top = AppSpacing.md),
            )
        }
    }
}

@Composable
private fun CompletedClassSummarySection(model: CompletedClassDetailUiModel) {
    val typography = appTypography()

    Column(
        modifier =
            Modifier
                .fillMaxWidth()
                .background(StuColors.Surface)
                .padding(
                    horizontal = AppSpacing.screenPadding,
                    vertical = AppSpacing.xxl,
                ),
        verticalArrangement = Arrangement.spacedBy(AppSpacing.lg),
    ) {
        ScheduleStatusChip(type = ScheduleStatusChipType.COMPLETED)
        Text(
            text = model.title,
            style = typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
            color = StuColors.TextPrimary,
        )
        Text(
            text = stringResource(Res.string.my_schedule_instructor_name, model.instructor.name),
            style = typography.bodyLarge,
            color = StuColors.TextSecondary,
        )
    }
}

@Composable
private fun CompletedClassInformationSection(model: CompletedClassDetailUiModel) {
    Column(
        modifier =
            Modifier
                .fillMaxWidth()
                .background(StuColors.Surface)
                .padding(AppSpacing.screenPadding),
        verticalArrangement = Arrangement.spacedBy(AppSpacing.xl),
    ) {
        SectionTitle(text = stringResource(Res.string.my_schedule_class_detail_information))
        CompletedClassInformationRow(
            icon = Res.drawable.ic_calendar_today,
            label = Res.string.my_schedule_class_detail_date,
            primaryValue = model.dateTime.dateLabel,
            secondaryValue =
                stringResource(
                    Res.string.my_schedule_class_detail_time_with_duration,
                    model.dateTime.timeRangeLabel,
                    model.durationMinutes,
                ),
            emphasizeSecondaryValue = true,
        )
        CompletedClassInformationRow(
            icon = Res.drawable.ic_location_on,
            label = Res.string.my_schedule_class_detail_location,
            primaryValue = model.location.name,
            secondaryValue = model.location.detail,
        )
        CompletedClassInformationRow(
            icon = Res.drawable.ic_confirmation_number,
            label = Res.string.my_schedule_class_detail_ticket,
            primaryValue = model.ticket.name,
            secondaryValue =
                stringResource(
                    Res.string.my_schedule_class_detail_ticket_counts,
                    model.ticket.remainingCount,
                    model.ticket.totalCount,
                ),
        )
    }
}

@Composable
private fun CompletedClassInformationRow(
    icon: DrawableResource,
    label: StringResource,
    primaryValue: String,
    secondaryValue: String,
    emphasizeSecondaryValue: Boolean = false,
) {
    val typography = appTypography()

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
                modifier = Modifier.padding(AppSpacing.sm).size(AppSpacing.xl),
                tint = StuColors.TextSecondary,
            )
        }
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(AppSpacing.xs),
        ) {
            Text(
                text = stringResource(label),
                style = typography.bodySmall,
                color = StuColors.TextSecondary,
            )
            Text(
                text = primaryValue,
                style = typography.bodyLarge.copy(fontWeight = FontWeight.Medium),
                color = StuColors.TextPrimary,
            )
            Text(
                text = secondaryValue,
                style = typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                color =
                    if (emphasizeSecondaryValue) {
                        StuColors.PrimaryGreen
                    } else {
                        StuColors.TextSecondary
                    },
            )
        }
    }
}

@Composable
private fun CompletedClassInstructorSection(
    model: CompletedClassDetailUiModel,
    onClick: () -> Unit,
) {
    val typography = appTypography()

    Column(
        modifier =
            Modifier
                .fillMaxWidth()
                .background(StuColors.Surface)
                .padding(AppSpacing.screenPadding),
        verticalArrangement = Arrangement.spacedBy(AppSpacing.lg),
    ) {
        SectionTitle(text = stringResource(Res.string.my_schedule_class_detail_instructor))
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .clickable(
                        role = Role.Button,
                        onClickLabel = stringResource(Res.string.my_schedule_class_detail_open_instructor),
                        onClick = onClick,
                    ),
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
                    text = stringResource(Res.string.my_schedule_instructor_name, model.instructor.name),
                    style = typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = StuColors.TextPrimary,
                )
                Text(
                    text = model.instructor.specialtyLabel,
                    style = typography.bodySmall,
                    color = StuColors.TextSecondary,
                )
            }
            Text(
                text = stringResource(Res.string.my_schedule_open_detail_mark),
                style = typography.titleLarge,
                color = StuColors.Divider,
            )
        }
    }
}

@Composable
private fun CompletedClassNoticeSection() {
    val notices =
        listOf(
            stringResource(Res.string.my_schedule_class_detail_history_condition),
            stringResource(Res.string.my_schedule_class_detail_ticket_inquiry),
        )

    Column(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(
                    horizontal = AppSpacing.screenPadding,
                    vertical = AppSpacing.xxl,
                ),
        verticalArrangement = Arrangement.spacedBy(AppSpacing.md),
    ) {
        SectionTitle(
            text = stringResource(Res.string.my_schedule_class_detail_notice),
            color = StuColors.TextSecondary,
        )
        notices.forEach { notice ->
            Row(horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm)) {
                Text(
                    text = stringResource(Res.string.my_schedule_bullet),
                    style = appTypography().bodySmall,
                    color = StuColors.TextTertiary,
                )
                Text(
                    text = notice,
                    modifier = Modifier.weight(1f),
                    style = appTypography().bodySmall,
                    color = StuColors.TextTertiary,
                )
            }
        }
    }
}

@Composable
private fun SectionTitle(
    text: String,
    color: androidx.compose.ui.graphics.Color = StuColors.TextPrimary,
) {
    Text(
        text = text,
        style = appTypography().titleSmall.copy(fontWeight = FontWeight.Bold),
        color = color,
    )
}

@Composable
private fun SectionGap() {
    Spacer(
        modifier =
            Modifier
                .fillMaxWidth()
                .height(AppSpacing.sm),
    )
}

@Preview(
    name = "Completed class detail content · Student · Default",
    group = "Component/MySchedule",
    showBackground = true,
    locale = "ko",
    widthDp = 391,
    heightDp = 843,
)
@Composable
private fun CompletedClassDetailContentPreview_Content_Student_Default() {
    AppTheme(theme = ThemeType.STUDENT) {
        CompletedClassDetailContent(
            model = completedClassDetailPreviewModel(),
            onOpenInstructor = {},
            onInquiry = {},
        )
    }
}
