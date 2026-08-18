package com.classitda.core.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.classitda.core.designsystem.AppSpacing
import com.classitda.core.designsystem.AppTheme
import com.classitda.core.designsystem.StuColors

@Composable
fun TopBar(
    title: String,
    hasBackground: Boolean,
    modifier: Modifier = Modifier,
) {
    val bgColor: Color = if (hasBackground) StuColors.White else StuColors.Background

    Box(
        modifier =
            modifier
                .height(52.dp)
                .fillMaxWidth()
                .background(bgColor)
                .padding(start = AppSpacing.screenPadding),
        contentAlignment = Alignment.CenterStart,
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.headlineSmall,
        )
    }
}

@Preview("흰 배경")
@Composable
private fun TopBarPreview1() {
    AppTheme {
        TopBar(
            title = "예약",
            hasBackground = true,
        )
    }
}

@Preview("배경 없음 = student 배경 색")
@Composable
private fun TopBarPreview2() {
    AppTheme {
        TopBar(
            title = "예약",
            hasBackground = false,
        )
    }
}
