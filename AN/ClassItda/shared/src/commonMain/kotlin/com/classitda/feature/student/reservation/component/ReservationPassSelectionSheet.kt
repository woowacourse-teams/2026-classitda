package com.classitda.feature.student.reservation.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.classitda.core.designsystem.AppShape
import com.classitda.core.designsystem.AppSpacing
import com.classitda.core.designsystem.AppTheme
import com.classitda.core.designsystem.StuColors
import com.classitda.feature.student.reservation.contract.ReservationPassUiModel

@Composable
@OptIn(ExperimentalMaterial3Api::class)
internal fun ReservationPassSelectionSheet(
    passes: List<ReservationPassUiModel>,
    selectedPassId: String?,
    onPassClick: (String) -> Unit,
    onDismissRequest: () -> Unit,
) {
    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        containerColor = StuColors.White,
    ) {
        ReservationPassSelectionContent(
            passes = passes,
            selectedPassId = selectedPassId,
            onPassClick = onPassClick,
        )
    }
}

@Composable
internal fun ReservationPassSelectionContent(
    passes: List<ReservationPassUiModel>,
    selectedPassId: String?,
    onPassClick: (String) -> Unit,
) {
    Column(
        modifier =
            Modifier
                .fillMaxWidth()
                .background(StuColors.White)
                .padding(
                    start = AppSpacing.screenPadding,
                    end = AppSpacing.screenPadding,
                    bottom = AppSpacing.xxl,
                ),
        verticalArrangement = Arrangement.spacedBy(AppSpacing.sm),
    ) {
        Text(
            text = "수강권 선택",
            color = StuColors.TextPrimary,
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            modifier = Modifier.fillMaxWidth(),
        )

        passes.forEach { pass ->
            val selected = pass.id == selectedPassId
            Card(
                modifier = Modifier.fillMaxWidth().clickable { onPassClick(pass.id) },
                shape = AppShape.Card,
                border =
                    BorderStroke(
                        width = 1.dp,
                        color = if (selected) StuColors.TextPrimary else StuColors.Divider,
                    ),
                colors = CardDefaults.cardColors(containerColor = StuColors.White),
            ) {
                Column(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .padding(horizontal = AppSpacing.cardPadding, vertical = 10.dp),
                    verticalArrangement = Arrangement.spacedBy(2.dp),
                ) {
                    Text(
                        text = pass.name,
                        color = StuColors.TextPrimary,
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                    )
                    Text(
                        text = pass.remainingText.toReservableCountText(),
                        color = StuColors.TextSecondary,
                        style = MaterialTheme.typography.bodySmall,
                    )
                    Text(
                        text = pass.validityPeriodText,
                        color = StuColors.TextTertiary,
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
            }
        }
    }
}

private fun String.toReservableCountText(): String = substringAfter("/ ", missingDelimiterValue = this)

private val previewPasses =
    listOf(
        ReservationPassUiModel("pass-1", "요가 10회권", "잔여 7회 / 예약 가능 7회", "유효기간: 2026.08.01 ~ 2026.09.30"),
        ReservationPassUiModel("pass-2", "필라테스 20회권", "잔여 12회 / 예약 가능 12회", "유효기간: 2026.08.01 ~ 2026.10.15"),
        ReservationPassUiModel("pass-3", "요가 / 필라테스 통합 1회권", "잔여 1회 / 예약 가능 1회", "유효기간: 없음"),
    )

@Preview(name = "수강권 선택 바텀시트", showBackground = true)
@Composable
private fun ReservationPassSelectionSheetPreview() {
    AppTheme {
        var selectedPassId by remember { mutableStateOf<String?>("pass-1") }

        ReservationPassSelectionContent(
            passes = previewPasses,
            selectedPassId = selectedPassId,
            onPassClick = { selectedPassId = it },
        )
    }
}
