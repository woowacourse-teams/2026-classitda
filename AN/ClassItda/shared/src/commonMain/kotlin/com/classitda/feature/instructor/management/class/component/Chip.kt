package com.classitda.feature.instructor.management.`class`.component

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.classitda.core.designsystem.AppShape
import com.classitda.core.designsystem.AppSpacing
import com.classitda.core.designsystem.InsColors

@Composable
fun Chip(
    text: String,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier,
        shape = AppShape.Pill,
        color = InsColors.SurfaceVariant,
        contentColor = InsColors.TextSecondary,
    ) {
        Text(
            text = text,
            modifier =
                Modifier.padding(
                    horizontal = AppSpacing.pillChipHorizontalPadding,
                    vertical = AppSpacing.pillChipVerticalPadding,
                ),
            style = MaterialTheme.typography.labelMedium,
        )
    }
}

@Composable
@Preview
private fun ChipPreview() {
    Chip(text = "필라테스")
}
