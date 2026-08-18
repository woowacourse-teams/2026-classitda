package com.classitda.feature.student.reservation.classreservation

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.classitda.core.designsystem.AppShape
import com.classitda.core.designsystem.AppSpacing
import com.classitda.core.designsystem.AppTheme
import com.classitda.core.designsystem.StuColors
import com.classitda.core.designsystem.component.PrimaryButton

internal data class SelectedClassUiModel(
    val id: String,
    val className: String,
    val dateText: String,
    val timeText: String,
    val instructorName: String,
    val memoText: String,
    val cancellationNotice: String,
)

internal data class ClassPassUiModel(
    val id: String,
    val name: String,
    val usageText: String,
    val validityPeriodText: String,
)

@Composable
internal fun ClassReservationScreen(
    selectedClass: SelectedClassUiModel,
    classPasses: List<ClassPassUiModel>,
    selectedPassId: String?,
    isTermsAgreed: Boolean,
    onBackClick: () -> Unit,
    onPassClick: (String) -> Unit,
    onTermsAgreementChange: (Boolean) -> Unit,
    onReservationClick: () -> Unit,
    timeConflict: ReservationTimeConflictUiModel? = null,
    onTimeConflictDismiss: () -> Unit = {},
    onScheduleClick: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier,
        containerColor = StuColors.Background,
        topBar = {
            ClassReservationTopBar(
                onBackClick = onBackClick,
            )
        },
        bottomBar = {
            ClassReservationBottomBar(
                enabled = selectedPassId != null && isTermsAgreed,
                onReservationClick = onReservationClick,
            )
        },
    ) { contentPadding ->
        LazyColumn(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(contentPadding)
                    .padding(horizontal = AppSpacing.screenPadding),
            verticalArrangement = Arrangement.spacedBy(AppSpacing.sectionGap),
        ) {
            item {
                Column(
                    verticalArrangement = Arrangement.spacedBy(AppSpacing.md),
                ) {
                    Text(
                        text = "선택한 수업",
                        color = StuColors.TextSecondary,
                        style = MaterialTheme.typography.labelLarge,
                        modifier = Modifier.padding(top = AppSpacing.md)
                    )

                    SelectedClassCard(
                        selectedClass = selectedClass,
                    )
                }
            }

            item {
                ClassPassSection(
                    classPasses = classPasses,
                    selectedPassId = selectedPassId,
                    onPassClick = onPassClick,
                )
            }

            item {
                ReservationTermsSection(
                    checked = isTermsAgreed,
                    onCheckedChange = onTermsAgreementChange,
                    modifier = Modifier.padding(bottom = AppSpacing.sectionGap),
                )
            }
        }
    }

    if (timeConflict != null) {
        ReservationTimeConflictDialog(
            conflict = timeConflict,
            onDismissRequest = onTimeConflictDismiss,
            onScheduleClick = onScheduleClick,
        )
    }
}

@Composable
private fun ClassReservationTopBar(
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .height(52.dp)
                .background(StuColors.White)
                .padding(horizontal = AppSpacing.screenPadding),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier =
                Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .clickable(onClick = onBackClick),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = "‹",
                color = StuColors.TextPrimary,
                style = MaterialTheme.typography.titleLarge,
            )
        }

        Text(
            text = "수업 예약",
            color = StuColors.TextPrimary,
            style = MaterialTheme.typography.titleMedium,
        )
    }
}

@Composable
private fun SelectedClassCard(
    selectedClass: SelectedClassUiModel,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = AppShape.Card,
        colors =
            CardDefaults.cardColors(
                containerColor = StuColors.White,
            ),
    ) {
        Column(
            modifier = Modifier.padding(AppSpacing.cardPadding),
            verticalArrangement = Arrangement.spacedBy(AppSpacing.sm),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = selectedClass.className,
                    color = StuColors.TextPrimary,
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                )

                Spacer(modifier = Modifier.weight(1f))

                Text(
                    text = selectedClass.dateText,
                    color = StuColors.TextSecondary,
                    style = MaterialTheme.typography.bodySmall,
                )
            }

            Text(
                text = selectedClass.timeText,
                color = StuColors.TextPrimary,
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
            )

            Text(
                text = selectedClass.instructorName,
                color = StuColors.TextSecondary,
                style = MaterialTheme.typography.bodySmall,
            )

            Text(
                text = selectedClass.memoText,
                color = StuColors.TextSecondary,
                style = MaterialTheme.typography.bodySmall,
            )

            Box(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .background(
                            color = StuColors.SurfaceVariant,
                            shape = AppShape.Card,
                        ).padding(AppSpacing.md),
            ) {
                Text(
                    text = "ⓘ  ${selectedClass.cancellationNotice}",
                    color = StuColors.TextSecondary,
                    style = MaterialTheme.typography.bodySmall,
                )
            }
        }
    }
}

@Composable
private fun ClassPassSection(
    classPasses: List<ClassPassUiModel>,
    selectedPassId: String?,
    onPassClick: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(AppSpacing.cardGap),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "사용할 수강권 선택",
                color = StuColors.TextSecondary,
                style = MaterialTheme.typography.labelLarge,
            )

            Spacer(modifier = Modifier.weight(1f))

            Text(
                text = "사용 가능한 수강권 ${classPasses.size}개",
                color = StuColors.TextTertiary,
                style = MaterialTheme.typography.labelMedium,
            )
        }

        classPasses.forEach { classPass ->
            ClassPassItem(
                classPass = classPass,
                selected = classPass.id == selectedPassId,
                onClick = {
                    onPassClick(classPass.id)
                },
            )
        }
    }
}

@Composable
private fun ClassPassItem(
    classPass: ClassPassUiModel,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier =
            modifier
                .fillMaxWidth()
                .selectable(
                    selected = selected,
                    role = Role.RadioButton,
                    onClick = onClick,
                ),
        shape = AppShape.Card,
        border =
            BorderStroke(
                width = if (selected) 2.dp else 1.dp,
                color =
                    if (selected) {
                        StuColors.TextPrimary
                    } else {
                        StuColors.Divider
                    },
            ),
        colors =
            CardDefaults.cardColors(
                containerColor = StuColors.White,
            ),
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(AppSpacing.cardPadding),
            verticalArrangement = Arrangement.spacedBy(AppSpacing.sm),
        ) {
            Text(
                text = classPass.name,
                color = StuColors.TextPrimary,
                style =
                    MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                    ),
            )

            Text(
                text = highlightedAvailabilityText(classPass.usageText),
                style = MaterialTheme.typography.bodySmall,
            )

            Text(
                text = classPass.validityPeriodText,
                color = StuColors.TextTertiary,
                style = MaterialTheme.typography.bodySmall,
            )
        }
    }
}

private fun highlightedAvailabilityText(text: String) = buildAnnotatedString {
    val start = text.indexOf("예약 가능")
    if (start < 0) {
        append(text)
        return@buildAnnotatedString
    }
    val end = text.indexOf(" |", start).takeIf { it >= 0 } ?: text.length
    withStyle(SpanStyle(color = StuColors.TextSecondary)) {
        append(text.substring(0, start))
        withStyle(SpanStyle(color = StuColors.Green, fontWeight = FontWeight.Medium)) {
            append(text.substring(start, end))
        }
        append(text.substring(end))
    }
}

@Composable
private fun ReservationTermsSection(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(AppSpacing.md),
    ) {
        HorizontalDivider(color = StuColors.Divider)

        Row(
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Checkbox(
                checked = checked,
                onCheckedChange = onCheckedChange,
                colors =
                    CheckboxDefaults.colors(
                        checkedColor = StuColors.PrimaryColor,
                    ),
            )

            Text(
                text = "유의사항 및 취소 규정 동의 (필수)",
                color = StuColors.TextPrimary,
                style = MaterialTheme.typography.labelLarge,
            )
        }

        Text(
            text =
                "수업 시작 4시간 전 이후 취소 시 수강권이 자동 차감됩니다. " +
                    "무단 결석 시에도 동일하게 적용되오니 주의 부탁드립니다.",
            color = StuColors.TextSecondary,
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.padding(horizontal = AppSpacing.lg),
        )
    }
}

@Composable
private fun ClassReservationBottomBar(
    enabled: Boolean,
    onReservationClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = StuColors.White,
    ) {
        PrimaryButton(
            text = "예약 완료하기",
            onClick = onReservationClick,
            enabled = enabled,
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(
                        horizontal = AppSpacing.screenPadding,
                        vertical = AppSpacing.md,
                    ),
        )
    }
}

private val previewSelectedClass =
    SelectedClassUiModel(
        id = "1",
        className = "리포머 베이직",
        dateText = "2026.08.08 (토)",
        timeText = "오전 10:00 - 10:50",
        instructorName = "이지은 강사",
        memoText = "오늘 꼭 수건 챙겨오세요~",
        cancellationNotice = "예약 취소 및 변경은 수업 시작 4시간 전까지 가능합니다.",
    )

private val previewClassPasses =
    listOf(
        ClassPassUiModel(
            id = "pass-1",
            name = "[그룹] 8:1 리포머/체어 10회권",
            usageText = "잔여 6회 | 예약 가능 2회 | 취소 가능 10회",
            validityPeriodText = "유효기간: 2026.08.01 ~ 2026.12.31",
        ),
        ClassPassUiModel(
            id = "pass-2",
            name = "[이벤트] 전종목 이용권",
            usageText = "무제한 이용 가능",
            validityPeriodText = "유효기간: 없음",
        ),
    )

@Preview
@Composable
private fun ClassReservationScreenPreview() {
    AppTheme {
        ClassReservationScreen(
            selectedClass = previewSelectedClass,
            classPasses = previewClassPasses,
            selectedPassId = "pass-1",
            isTermsAgreed = true,
            onBackClick = {},
            onPassClick = {},
            onTermsAgreementChange = {},
            onReservationClick = {},
        )
    }
}
