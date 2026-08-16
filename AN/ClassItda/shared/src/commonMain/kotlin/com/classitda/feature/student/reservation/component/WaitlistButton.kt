package com.classitda.feature.student.reservation.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.classitda.core.designsystem.AppSpacing
import com.classitda.core.designsystem.AppTheme
import com.classitda.core.designsystem.StuColors

@Composable
fun WaitlistButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier
            .widthIn(min = 64.dp)
            .height(32.dp),
        shape = RoundedCornerShape(6.dp),
        border = BorderStroke(
            width = 1.dp,
            color = StuColors.Green,
        ),
        contentPadding = PaddingValues(horizontal = AppSpacing.md),
        colors = ButtonDefaults.buttonColors(
            containerColor = StuColors.White,
            contentColor = StuColors.Green,
        ),
    ) {
        Text(
            text = "대기 예약",
            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
        )
    }
}

@Preview
@Composable
private fun WaitlistButtonPreview() {
    AppTheme {
        WaitlistButton(
            onClick = {},
            modifier = Modifier,
        )
    }
}
