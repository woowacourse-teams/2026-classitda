package com.classitda.feature.student.myschedule.component.detail.waitlist

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import classitda.shared.generated.resources.Res
import classitda.shared.generated.resources.ic_calendar_today
import classitda.shared.generated.resources.ic_chat_bubble_outline
import classitda.shared.generated.resources.ic_confirmation_number
import classitda.shared.generated.resources.ic_schedule
import classitda.shared.generated.resources.my_schedule_bullet
import classitda.shared.generated.resources.my_schedule_approve_waitlist
import classitda.shared.generated.resources.my_schedule_cancel_waitlist
import classitda.shared.generated.resources.my_schedule_class_detail_date
import classitda.shared.generated.resources.my_schedule_class_detail_information
import classitda.shared.generated.resources.my_schedule_class_detail_instructor_information
import classitda.shared.generated.resources.my_schedule_class_detail_notice
import classitda.shared.generated.resources.my_schedule_class_detail_waitlist_confirmation_policy
import classitda.shared.generated.resources.my_schedule_instructor_avatar
import classitda.shared.generated.resources.my_schedule_memo
import classitda.shared.generated.resources.my_schedule_pass_cancellable
import classitda.shared.generated.resources.my_schedule_pass_reservable
import classitda.shared.generated.resources.my_schedule_pass_total_remaining
import classitda.shared.generated.resources.my_schedule_separator
import classitda.shared.generated.resources.my_schedule_status_approval_required
import classitda.shared.generated.resources.my_schedule_status_waitlist
import classitda.shared.generated.resources.my_schedule_used_ticket
import classitda.shared.generated.resources.my_schedule_waitlist_applied_at
import classitda.shared.generated.resources.my_schedule_waitlist_position_description
import classitda.shared.generated.resources.my_schedule_waitlist_position_title
import classitda.shared.generated.resources.my_schedule_waitlist_position_value
import com.classitda.core.designsystem.AppShape
import com.classitda.core.designsystem.AppSpacing
import com.classitda.core.designsystem.AppTheme
import com.classitda.core.designsystem.StuColors
import com.classitda.core.designsystem.ThemeType
import com.classitda.core.designsystem.appTypography
import com.classitda.feature.student.myschedule.component.common.MyScheduleWarningButton
import com.classitda.feature.student.myschedule.component.common.MySchedulePrimaryButton
import com.classitda.feature.student.myschedule.contract.WaitlistDetailUiModel
import com.classitda.feature.student.myschedule.contract.WaitlistDetailStatusUiModel
import com.classitda.feature.student.myschedule.preview.WaitlistDetailPreviewFixture
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun WaitlistDetailContent(
    model: WaitlistDetailUiModel,
    onCancelWaitlist: (() -> Unit)?,
    onApproveWaitlist: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
    listState: LazyListState = rememberLazyListState(),
) {
    LazyColumn(
        state = listState,
        modifier =
            modifier
                .fillMaxSize()
                .background(StuColors.Background),
    ) {
        item { WaitlistDetailSummary(model = model) }
        item { WaitlistSectionHeader(stringResource(Res.string.my_schedule_class_detail_information)) }
        item { WaitlistClassInfoSection(model = model) }
        item {
            WaitlistSectionHeader(
                stringResource(Res.string.my_schedule_class_detail_instructor_information),
            )
        }
        item { WaitlistInstructorSection(model = model) }
        item {
            WaitlistDetailFooter(
                onCancelWaitlist = onCancelWaitlist,
                onApproveWaitlist = onApproveWaitlist,
            )
        }
    }
}

@Composable
private fun WaitlistDetailSummary(model: WaitlistDetailUiModel) {
    Column(
        modifier =
            Modifier
                .fillMaxWidth()
                .background(StuColors.Surface)
                .padding(
                    horizontal = AppSpacing.screenPadding,
                    vertical = AppSpacing.xxl,
                ),
        verticalArrangement = Arrangement.spacedBy(AppSpacing.xxl),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top,
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(AppSpacing.lg),
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = stringResource(Res.string.my_schedule_bullet),
                        style = appTypography().titleMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = StuColors.Orange,
                    )
                    Text(
                        text =
                            stringResource(
                                when (model.status) {
                                    WaitlistDetailStatusUiModel.APPROVAL_REQUIRED -> {
                                        Res.string.my_schedule_status_approval_required
                                    }

                                    WaitlistDetailStatusUiModel.WAITLISTED -> {
                                        Res.string.my_schedule_status_waitlist
                                    }
                                },
                            ),
                        style = appTypography().titleMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = StuColors.Orange,
                    )
                }
                Text(
                    text = model.title,
                    modifier = Modifier.semantics { heading() },
                    style = appTypography().headlineSmall.copy(fontWeight = FontWeight.Bold),
                    color = StuColors.TextPrimary,
                )
            }
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(AppSpacing.xs),
            ) {
                Text(
                    text = stringResource(Res.string.my_schedule_waitlist_position_title),
                    style = appTypography().bodySmall,
                    color = StuColors.TextTertiary,
                )
                Text(
                    text =
                        stringResource(
                            Res.string.my_schedule_waitlist_position_value,
                            model.currentPosition,
                        ),
                    style = appTypography().titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = StuColors.Orange,
                )
            }
        }
        WaitlistInformationRow(
            icon = Res.drawable.ic_schedule,
            label = stringResource(Res.string.my_schedule_waitlist_applied_at),
        ) {
            Text(
                text = model.appliedAtLabel,
                style = appTypography().bodyLarge.copy(fontWeight = FontWeight.Medium),
                color = StuColors.TextPrimary,
            )
        }
    }
}

@Composable
private fun WaitlistClassInfoSection(model: WaitlistDetailUiModel) {
    Column(
        modifier =
            Modifier
                .fillMaxWidth()
                .background(StuColors.Surface)
                .padding(AppSpacing.screenPadding),
        verticalArrangement = Arrangement.spacedBy(AppSpacing.xxl),
    ) {
        WaitlistInformationRow(
            icon = Res.drawable.ic_calendar_today,
            label = stringResource(Res.string.my_schedule_class_detail_date),
        ) {
            Text(
                text = model.classInfo.dateLabel,
                style = appTypography().bodyLarge.copy(fontWeight = FontWeight.Medium),
                color = StuColors.TextPrimary,
            )
            Text(
                text = model.classInfo.timeRangeLabel,
                style = appTypography().bodyMedium,
                color = StuColors.TextSecondary,
            )
        }
        WaitlistInformationRow(
            icon = Res.drawable.ic_confirmation_number,
            label = stringResource(Res.string.my_schedule_used_ticket),
        ) {
            Text(
                text = model.pass.name,
                style = appTypography().bodyLarge.copy(fontWeight = FontWeight.SemiBold),
                color = StuColors.TextPrimary,
            )
            Text(
                text = model.pass.validityLabel,
                style = appTypography().bodyMedium,
                color = StuColors.TextSecondary,
            )
            Row(
                horizontalArrangement = Arrangement.spacedBy(AppSpacing.xs),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                WaitlistPassCount(
                    stringResource(Res.string.my_schedule_pass_total_remaining, model.pass.remainingUses),
                )
                WaitlistPassCountSeparator()
                WaitlistPassCount(
                    stringResource(Res.string.my_schedule_pass_reservable, model.pass.reservableUses),
                )
                WaitlistPassCountSeparator()
                WaitlistPassCount(
                    text =
                        stringResource(
                            Res.string.my_schedule_pass_cancellable,
                            model.pass.cancellableUses,
                        ),
                    color = StuColors.Red,
                )
            }
        }
        model.classInfo.memo?.let { memo ->
            WaitlistInformationRow(
                icon = Res.drawable.ic_chat_bubble_outline,
                label = stringResource(Res.string.my_schedule_memo),
            ) {
                Text(
                    text = memo,
                    style = appTypography().bodyMedium,
                    color = StuColors.TextPrimary,
                )
            }
        }
    }
}

@Composable
private fun WaitlistInstructorSection(model: WaitlistDetailUiModel) {
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

@Composable
private fun WaitlistDetailFooter(
    onCancelWaitlist: (() -> Unit)?,
    onApproveWaitlist: (() -> Unit)?,
) {
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
        Text(
            text = stringResource(Res.string.my_schedule_class_detail_notice),
            style = appTypography().titleSmall.copy(fontWeight = FontWeight.Medium),
            color = StuColors.TextSecondary,
        )
        WaitlistNotice(stringResource(Res.string.my_schedule_waitlist_position_description))
        WaitlistNotice(
            stringResource(Res.string.my_schedule_class_detail_waitlist_confirmation_policy),
        )
        MyScheduleWarningButton(
            text = stringResource(Res.string.my_schedule_cancel_waitlist),
            onClick = { onCancelWaitlist?.invoke() },
            modifier = Modifier.padding(top = AppSpacing.xxl),
            enabled = onCancelWaitlist != null,
        )
        onApproveWaitlist?.let { onApprove ->
            MySchedulePrimaryButton(
                text = stringResource(Res.string.my_schedule_approve_waitlist),
                onClick = onApprove,
            )
        }
    }
}

@Composable
private fun WaitlistNotice(text: String) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm),
        verticalAlignment = Alignment.Top,
    ) {
        Text(
            text = stringResource(Res.string.my_schedule_bullet),
            style = appTypography().bodySmall,
            color = StuColors.TextTertiary,
        )
        Text(
            text = text,
            modifier = Modifier.weight(1f),
            style = appTypography().bodySmall,
            color = StuColors.TextTertiary,
        )
    }
}

@Composable
private fun WaitlistSectionHeader(title: String) {
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

@Composable
private fun WaitlistInformationRow(
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

@Composable
private fun WaitlistPassCount(
    text: String,
    color: Color = StuColors.TextSecondary,
) {
    Text(
        text = text,
        style = appTypography().bodySmall,
        color = color,
    )
}

@Composable
private fun WaitlistPassCountSeparator() {
    Text(
        text = stringResource(Res.string.my_schedule_separator),
        style = appTypography().bodySmall,
        color = StuColors.DividerStrong,
    )
}

@Preview(
    name = "F06 pending / Student / Default",
    group = "Component/MySchedule/WaitlistDetail",
    showBackground = true,
    locale = "ko",
    widthDp = 390,
    heightDp = 788,
)
@Composable
private fun WaitlistDetailContentPreview_F06Pending_Student_Default() {
    AppTheme(theme = ThemeType.STUDENT) {
        WaitlistDetailContent(
            model = WaitlistDetailPreviewFixture.pending,
            onCancelWaitlist = {},
            onApproveWaitlist = null,
        )
    }
}

@Preview(
    name = "F06 approval required / Student / Default",
    group = "Component/MySchedule/WaitlistDetail",
    showBackground = true,
    locale = "ko",
    widthDp = 390,
    heightDp = 788,
)
@Composable
private fun WaitlistDetailContentPreview_F06ApprovalRequired_Student_Default() {
    AppTheme(theme = ThemeType.STUDENT) {
        WaitlistDetailContent(
            model = WaitlistDetailPreviewFixture.approvalRequired,
            onCancelWaitlist = {},
            onApproveWaitlist = {},
        )
    }
}
