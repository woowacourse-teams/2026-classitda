package com.classitda.feature.student.home.component

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import com.classitda.core.designsystem.StuColors

@Composable
fun SectionTitle(title: String) {
    Text(
        text = title,
        style =
            MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.SemiBold,
                color = StuColors.TextPrimary,
            ),
    )
}

@Composable
@Preview
private fun SectionTitlePreview() {
    SectionTitle(
        "다가오는 예약",
    )
}
