package com.classitda.feature.student.mypage.holding

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import classitda.shared.generated.resources.Res
import classitda.shared.generated.resources.ic_check_circle
import classitda.shared.generated.resources.ic_info
import com.classitda.core.designsystem.AppShape
import com.classitda.core.designsystem.AppSpacing
import com.classitda.core.designsystem.AppTheme
import com.classitda.core.designsystem.StuColors
import com.classitda.core.designsystem.ThemeType
import com.classitda.core.designsystem.component.PrimaryButton
import com.classitda.feature.student.mypage.holding.model.MyPassHoldingCompletedUiModel
import org.jetbrains.compose.resources.painterResource

@Composable
fun MyPassHoldingCompletedScreen(
    uiModel: MyPassHoldingCompletedUiModel,
    onReturnToDetailClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxSize().background(StuColors.Background),
    ) {
        Column(
            modifier =
                Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(AppSpacing.screenPadding),
            verticalArrangement = Arrangement.spacedBy(space = AppSpacing.xxl, alignment = Alignment.CenterVertically),
        ) {
            MyPassHoldingCompletedHero()
            MyPassHoldingCompletedDetailCard(uiModel = uiModel)
        }
        PrimaryButton(
            text = "확인",
            onClick = onReturnToDetailClick,
            modifier = Modifier.fillMaxWidth().padding(AppSpacing.screenPadding),
        )
    }
}

@Composable
private fun MyPassHoldingCompletedHero(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(AppSpacing.md),
    ) {
        Icon(
            painter = painterResource(Res.drawable.ic_check_circle),
            contentDescription = null,
            tint = StuColors.Black,
            modifier = Modifier.size(64.dp),
        )
        Text(
            text = "홀딩 요청이 접수됐어요",
            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
            color = StuColors.TextPrimary,
            textAlign = TextAlign.Center,
        )
        Text(
            text = "강사 확인 후 최종 확정됩니다.\n승인이 완료되면 알림으로 안내해 드릴게요.",
            style = MaterialTheme.typography.bodyMedium,
            color = StuColors.TextSecondary,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun MyPassHoldingCompletedDetailCard(
    uiModel: MyPassHoldingCompletedUiModel,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier =
            modifier
                .fillMaxWidth()
                .clip(AppShape.Card)
                .background(StuColors.Surface)
                .padding(AppSpacing.cardPadding),
    ) {
        Text(
            text = "요청 상세 내용",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = StuColors.TextPrimary,
        )
        Spacer(modifier = Modifier.size(AppSpacing.sectionGap))
        MyPassHoldingCompletedRow(
            label = "요청 기간",
            value = uiModel.requestPeriodLabel,
        )
        Spacer(modifier = Modifier.size(AppSpacing.xl))

        MyPassHoldingCompletedRow(
            label = "총 홀딩 일수",
            value = uiModel.totalHoldingDaysLabel,
        )
        Spacer(modifier = Modifier.size(AppSpacing.xl))

        MyPassHoldingCompletedRow(
            label = "변경 예정 만료일",
            value = "${uiModel.currentExpireDateLabel} → ${uiModel.newExpireDateLabel}",
        )

        Spacer(modifier = Modifier.size(AppSpacing.sectionGap))

        MyPassHoldingCompletedNotice()
    }
}

@Composable
private fun MyPassHoldingCompletedRow(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = StuColors.TextSecondary,
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            color = StuColors.TextPrimary,
        )
    }
}

@Composable
private fun MyPassHoldingCompletedNotice(modifier: Modifier = Modifier) {
    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .clip(AppShape.Card)
                .background(StuColors.SurfaceVariant)
                .padding(AppSpacing.lg),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            painter = painterResource(Res.drawable.ic_info),
            contentDescription = null,
            tint = StuColors.TextSecondary,
            modifier = Modifier.size(16.dp),
        )
        Text(
            text = "승인 전까지는 기존 일정대로 수업 예약이 가능합니다.",
            modifier = Modifier.padding(start = AppSpacing.sm),
            style = MaterialTheme.typography.bodySmall,
            color = StuColors.TextSecondary,
        )
    }
}

private val previewUiModel =
    MyPassHoldingCompletedUiModel(
        requestPeriodLabel = "2026.08.10(월) ~ 2026.08.23(금)",
        totalHoldingDaysLabel = "14일",
        currentExpireDateLabel = "2026.10.01(월)",
        newExpireDateLabel = "2026.10.14(일)",
    )

@Preview
@Composable
private fun MyPassHoldingCompletedScreenPreview() {
    AppTheme(theme = ThemeType.STUDENT) {
        MyPassHoldingCompletedScreen(
            uiModel = previewUiModel,
            onReturnToDetailClick = {},
        )
    }
}
