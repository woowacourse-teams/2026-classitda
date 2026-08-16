package com.classitda.feature.student.reservation.waitlist

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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.classitda.core.designsystem.AppShape
import com.classitda.core.designsystem.AppSpacing
import com.classitda.core.designsystem.AppTheme
import com.classitda.core.designsystem.StuColors
import com.classitda.core.designsystem.component.PrimaryButton

internal data class WaitlistClassUiModel(
    val id: String,
    val className: String,
    val dateText: String,
    val timeText: String,
    val instructorName: String,
    val roomName: String,
    val memoText: String,
    val cancellationNotice: String,
)

internal data class WaitlistClassPassUiModel(
    val id: String,
    val name: String,
    val usageText: String,
    val expirationText: String,
)

@Composable
internal fun WaitlistReservationScreen(
    selectedClass: WaitlistClassUiModel,
    classPasses: List<WaitlistClassPassUiModel>,
    selectedPassId: String?,
    expectedWaitingNumber: Int,
    onBackClick: () -> Unit,
    onPassClick: (String) -> Unit,
    onApplyClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier,
        containerColor = StuColors.Background,
        topBar = {
            WaitlistReservationTopBar(onBackClick = onBackClick)
        },
        bottomBar = {
            WaitlistReservationBottomBar(
                enabled = selectedPassId != null,
                onApplyClick = onApplyClick,
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
                Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.md)) {
                    Text(
                        text = "선택한 수업",
                        color = StuColors.TextSecondary,
                        style = MaterialTheme.typography.labelLarge,
                    )
                    SelectedWaitlistClassCard(selectedClass = selectedClass)
                }
            }

            item {
                WaitlistPassSection(
                    classPasses = classPasses,
                    selectedPassId = selectedPassId,
                    onPassClick = onPassClick,
                )
            }

            item {
                ExpectedWaitingNumber(number = expectedWaitingNumber)
            }

            item {
                WaitlistGuide(
                    modifier = Modifier.padding(bottom = AppSpacing.sectionGap),
                )
            }
        }
    }
}

@Composable
private fun WaitlistReservationTopBar(
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
            text = "대기 예약",
            color = StuColors.TextPrimary,
            style = MaterialTheme.typography.titleMedium,
        )
    }
}

@Composable
private fun SelectedWaitlistClassCard(
    selectedClass: WaitlistClassUiModel,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = AppShape.Card,
        colors = CardDefaults.cardColors(containerColor = StuColors.White),
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
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
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
                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
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
            Text(
                text = "수업 ID ${selectedClass.id}",
                color = StuColors.TextTertiary,
                style = MaterialTheme.typography.labelSmall,
            )
            Box(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .background(StuColors.SurfaceVariant, AppShape.Card)
                        .padding(AppSpacing.md),
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
private fun WaitlistPassSection(
    classPasses: List<WaitlistClassPassUiModel>,
    selectedPassId: String?,
    onPassClick: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(AppSpacing.cardGap),
    ) {
        Row(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = "사용할 수강권 선택",
                color = StuColors.TextSecondary,
                style = MaterialTheme.typography.labelLarge,
            )
            Spacer(modifier = Modifier.weight(1f))
            Text(
                text = "사용 가능한 수강권 ${classPasses.size}개",
                color = StuColors.TextSecondary,
                style = MaterialTheme.typography.labelSmall,
            )
        }

        classPasses.forEach { classPass ->
            val selected = classPass.id == selectedPassId
            Card(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .selectable(
                            selected = selected,
                            role = Role.RadioButton,
                            onClick = { onPassClick(classPass.id) },
                        ),
                shape = AppShape.Card,
                border =
                    BorderStroke(
                        width = if (selected) 2.dp else 1.dp,
                        color = if (selected) StuColors.TextPrimary else StuColors.Divider,
                    ),
                colors = CardDefaults.cardColors(containerColor = StuColors.White),
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(AppSpacing.cardPadding),
                    verticalArrangement = Arrangement.spacedBy(AppSpacing.sm),
                ) {
                    Text(
                        text = classPass.name,
                        color = StuColors.TextPrimary,
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                    )
                    Text(
                        text = highlightedAvailabilityText(classPass.usageText),
                        style = MaterialTheme.typography.bodySmall,
                    )
                    Text(
                        text = classPass.expirationText,
                        color = StuColors.TextTertiary,
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
            }
        }
    }
}

private fun highlightedAvailabilityText(text: String) = buildAnnotatedString {
    val start = text.indexOf("예약 가능")
    if (start < 0) {
        append(text)
        return@buildAnnotatedString
    }
    val end = text.indexOf(" /", start).takeIf { it >= 0 } ?: text.length
    withStyle(SpanStyle(color = StuColors.TextSecondary)) {
        append(text.substring(0, start))
        withStyle(SpanStyle(color = StuColors.Green, fontWeight = FontWeight.Medium)) {
            append(text.substring(start, end))
        }
        append(text.substring(end))
    }
}

@Composable
private fun ExpectedWaitingNumber(
    number: Int,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxWidth()) {
        HorizontalDivider(color = StuColors.Divider)
        Row(
            modifier = Modifier.fillMaxWidth().padding(vertical = AppSpacing.lg),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "예상 대기 번호",
                color = StuColors.TextSecondary,
                style = MaterialTheme.typography.labelLarge,
            )
            Spacer(modifier = Modifier.weight(1f))
            Text(
                text = "${number}번",
                color = StuColors.Orange,
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                textAlign = TextAlign.End,
            )
        }
        HorizontalDivider(color = StuColors.Divider)
    }
}

@Composable
private fun WaitlistGuide(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(AppSpacing.md),
    ) {
        Text(
            text = "· 기존 예약자가 취소할 경우 대기 순번에 따라 자동으로 예약이 확정됩니다.",
            color = StuColors.TextSecondary,
            style = MaterialTheme.typography.bodySmall,
        )
        Text(
            text = "· 수업 시작 2시간 전까지 자리가 나지 않을 경우 대기 예약은 자동으로 소멸됩니다.",
            color = StuColors.TextSecondary,
            style = MaterialTheme.typography.bodySmall,
        )
    }
}

@Composable
private fun WaitlistReservationBottomBar(
    enabled: Boolean,
    onApplyClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = StuColors.White,
    ) {
        PrimaryButton(
            text = "대기 예약 신청하기",
            onClick = onApplyClick,
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

@Preview(name = "대기 예약 화면")
@Composable
private fun WaitlistReservationScreenPreview() {
    AppTheme {
        WaitlistReservationScreen(
            selectedClass =
                WaitlistClassUiModel(
                    id = "2",
                    className = "리포머 베이직",
                    dateText = "2026.08.08 (토)",
                    timeText = "오전 10:00 - 10:50",
                    instructorName = "이지은 강사",
                    roomName = "리포머룸",
                    memoText = "오늘 꼭 수건 챙겨오세요~",
                    cancellationNotice = "예약 취소 및 변경은 수업 시작 4시간 전까지 가능합니다.",
                ),
            classPasses =
                listOf(
                    WaitlistClassPassUiModel(
                        id = "pass-1",
                        name = "[그룹] 8:1 리포머/체어 10회권",
                        usageText = "잔여 6회 / 예약 가능 2회 / 취소 가능 10회",
                        expirationText = "2026.12.31까지",
                    ),
                    WaitlistClassPassUiModel(
                        id = "pass-2",
                        name = "[이벤트] 한정판 이용권",
                        usageText = "잔여 1회 / 예약 가능 1회 / 취소 가능 10회",
                        expirationText = "2026.11.30까지",
                    ),
                ),
            selectedPassId = "pass-1",
            expectedWaitingNumber = 3,
            onBackClick = {},
            onPassClick = {},
            onApplyClick = {},
        )
    }
}
