package com.classitda.feature.student.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import classitda.shared.generated.resources.Res
import classitda.shared.generated.resources.ic_arrow_forward
import classitda.shared.generated.resources.ic_info
import com.classitda.core.designsystem.AppShape
import com.classitda.core.designsystem.AppSpacing
import com.classitda.core.designsystem.AppTheme
import com.classitda.core.designsystem.StuColors
import com.classitda.feature.student.home.component.SectionTitle
import org.jetbrains.compose.resources.painterResource

@Composable
fun MyPassesSection(
    passName: String,
    expireDateText: String,
    totalRemaining: Int,
    reservable: Int,
    cancellable: Int,
    onInfoClick: () -> Unit,
    onPassCardClick: () -> Unit,
    onSeeAllClick: () -> Unit,
    modifier: Modifier = Modifier,
    holdingPeriod: String? = null,
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(
                modifier = Modifier.clickable(onClick = onInfoClick),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                SectionTitle("내 수강권")
                Spacer(modifier = Modifier.width(AppSpacing.xs))
                Icon(
                    painter = painterResource(Res.drawable.ic_info),
                    contentDescription = null,
                    tint = StuColors.TextTertiary,
                    modifier = Modifier.size(16.dp),
                )
            }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(vertical = AppSpacing.xs).clickable(onClick = onSeeAllClick),
            ) {
                Text(
                    text = "전체 보기",
                    style = MaterialTheme.typography.labelLarge.copy(fontSize = 13.sp),
                    color = StuColors.TextSecondary,
                )
                Icon(
                    painter = painterResource(Res.drawable.ic_arrow_forward),
                    contentDescription = null,
                    tint = StuColors.TextSecondary,
                    modifier = Modifier.padding(start = AppSpacing.xs).size(12.dp),
                )
            }
        }

        Spacer(Modifier.height(AppSpacing.sm))

        PassCard(
            passName = passName,
            expireDate = expireDateText,
            totalRemaining = totalRemaining,
            reservable = reservable,
            cancellable = cancellable,
            onClick = onPassCardClick,
            holdingPeriod = holdingPeriod,
        )
    }
}

@Composable
private fun PassCard(
    passName: String,
    expireDate: String,
    totalRemaining: Int,
    reservable: Int,
    cancellable: Int,
    onClick: () -> Unit,
    holdingPeriod: String? = null,
) {
    Column(
        modifier =
            Modifier
                .clickable(onClick = onClick)
                .fillMaxWidth()
                .background(StuColors.Surface, AppShape.Card)
                .padding(AppSpacing.cardPadding),
        verticalArrangement = Arrangement.spacedBy(AppSpacing.sm),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = passName,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, fontSize = 18.sp),
                color = StuColors.TextPrimary,
            )

            Text(
                text = expireDate,
                style = MaterialTheme.typography.bodySmall,
                color = StuColors.TextSecondary,
            )
        }

        holdingPeriod?.let { holdingPeriod ->
            Text(
                text = "홀딩 기간: $holdingPeriod",
                style = MaterialTheme.typography.bodySmall,
                color = StuColors.Red,
            )
        }

        HorizontalDivider(
            thickness = 1.dp,
            color = StuColors.Divider,
            modifier = Modifier.padding(AppSpacing.sm),
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            PassMetric(
                label = "전체 잔여",
                value = "${totalRemaining}회",
                valueColor = StuColors.TextPrimary,
                modifier = Modifier.weight(1f),
            )
            VerticalDivider(thickness = 1.dp, modifier = Modifier.height(12.dp), color = StuColors.DividerStrong)
            PassMetric(
                label = "예약 가능",
                value = "${reservable}회",
                valueColor = StuColors.Black,
                modifier = Modifier.weight(1f),
            )
            VerticalDivider(thickness = 1.dp, modifier = Modifier.height(12.dp), color = StuColors.DividerStrong)
            PassMetric(
                label = "취소 가능",
                value = "${cancellable}회",
                valueColor = StuColors.TextPrimary,
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun PassMetric(
    label: String,
    value: String,
    valueColor: Color,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = StuColors.TextSecondary,
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
            color = valueColor,
            modifier = Modifier.padding(start = AppSpacing.xs),
        )
    }
}

@Composable
@Preview(showBackground = true, backgroundColor = 0xffF4F6F5)
private fun MyPassesSectionPreview() {
    AppTheme {
        MyPassesSection(
            passName = "리포머 20회권",
            expireDateText = "2026.09.30까지",
            totalRemaining = 8,
            reservable = 5,
            cancellable = 2,
            onInfoClick = {},
            onPassCardClick = {},
            onSeeAllClick = {},
        )
    }
}

@Composable
@Preview
private fun PassCardHoldingPreview() {
    AppTheme {
        PassCard(
            passName = "리포머 20회권",
            expireDate = "2026.09.30까지",
            totalRemaining = 8,
            reservable = 5,
            cancellable = 2,
            onClick = {},
            holdingPeriod = "2026.19.29 ~ 2026.19.34",
        )
    }
}
