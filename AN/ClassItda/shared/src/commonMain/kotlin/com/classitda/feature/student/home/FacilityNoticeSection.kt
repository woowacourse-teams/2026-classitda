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
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import classitda.shared.generated.resources.Res
import classitda.shared.generated.resources.ic_arrow_forward
import com.classitda.core.designsystem.AppShape
import com.classitda.core.designsystem.AppSpacing
import com.classitda.core.designsystem.AppTheme
import com.classitda.core.designsystem.StuColors
import com.classitda.feature.student.home.component.SectionTitle
import org.jetbrains.compose.resources.painterResource

@Composable
fun FacilityNoticeSection(
    title: String,
    description: String,
    date: String,
    onNoticeClick: () -> Unit,
    onSeeAllClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            SectionTitle("시설 공지")
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

        FacilityNoticeCard(
            title = title,
            description = description,
            dateText = date,
            onClick = onNoticeClick,
        )
    }
}

@Composable
private fun FacilityNoticeCard(
    title: String,
    description: String,
    dateText: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .clickable(onClick = onClick)
                .background(StuColors.Surface, AppShape.Card)
                .padding(AppSpacing.cardPadding),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(AppSpacing.sm),
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = StuColors.TextPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium,
                color = StuColors.TextSecondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = dateText,
                style = MaterialTheme.typography.bodySmall,
                color = StuColors.TextTertiary,
            )
        }

        Icon(
            painter = painterResource(Res.drawable.ic_arrow_forward),
            contentDescription = null,
            tint = StuColors.TextTertiary,
            modifier = Modifier.padding(start = AppSpacing.sm).size(20.dp),
        )
    }
}

@Composable
@Preview(showBackground = true, backgroundColor = 0xffF4F6F5)
private fun FacilityNoticeSectionPreview() {
    AppTheme {
        FacilityNoticeSection(
            title = "샤워실 이용 시간이 변경되었어요",
            description = "8월 10일부터 평일 샤워실은 오후 10시까지 운영...",
            date = "2026.08.05",
            onNoticeClick = {},
            onSeeAllClick = {},
        )
    }
}
