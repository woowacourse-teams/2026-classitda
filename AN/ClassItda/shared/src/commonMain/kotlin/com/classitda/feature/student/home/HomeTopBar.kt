package com.classitda.feature.student.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import classitda.shared.generated.resources.Res
import classitda.shared.generated.resources.ic_arrow_down
import classitda.shared.generated.resources.ic_notification
import com.classitda.core.designsystem.AppSpacing
import com.classitda.core.designsystem.StuColors
import org.jetbrains.compose.resources.painterResource

@Composable
fun HomeTopBar(
    studioName: String,
    onStudioClick: () -> Unit,
    onNotificationClick: () -> Unit,
    modifier: Modifier = Modifier,
    hasNewNotification: Boolean = false,
) {
    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .background(StuColors.Background)
                .statusBarsPadding()
                .height(52.dp)
                .padding(horizontal = AppSpacing.screenPadding),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        StudioSection(
            studioName = studioName,
            onClick = onStudioClick,
        )

        Box(
            modifier =
                Modifier
                    .size(48.dp)
                    .clickable(onClick = onNotificationClick)
                    .padding(AppSpacing.xs)
                    .clip(CircleShape)
                    .background(StuColors.White)
                    .padding(8.dp),
        ) {
            Icon(
                painter = painterResource(Res.drawable.ic_notification),
                contentDescription = "알림",
                tint = StuColors.TextPrimary,
            )
            if (hasNewNotification) {
                Box(
                    modifier =
                        Modifier
                            .size(AppSpacing.xs)
                            .clip(CircleShape)
                            .background(StuColors.Green)
                            .align(Alignment.TopEnd),
                )
            }
        }
    }
}

@Composable
private fun StudioSection(
    studioName: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.padding(vertical = AppSpacing.xs).clickable(onClick = onClick),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(AppSpacing.xs),
    ) {
        Text(
            text = studioName,
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = StuColors.TextPrimary,
        )
        Icon(
            painter = painterResource(Res.drawable.ic_arrow_down),
            contentDescription = "센터 선택",
            modifier = Modifier.size(20.dp),
            tint = StuColors.TextSecondary,
        )
    }
}

@Composable
@Preview
private fun HomeTopBarPreView() {
    HomeTopBar(
        studioName = "코코필라테스&필라테스 영등점",
        onStudioClick = {},
        hasNewNotification = true,
        onNotificationClick = {},
    )
}
