package com.classitda.feature.student.home.component

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.classitda.core.designsystem.AppShape
import com.classitda.core.designsystem.AppTheme
import com.classitda.core.designsystem.StuColors

@Composable
fun PrimaryTextButton(
    content: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    contentPadding: PaddingValues = PaddingValues(vertical = 8.dp, horizontal = 16.dp),
) {
    Button(
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        shape = AppShape.Card,
        colors =
            ButtonDefaults.buttonColors(
                containerColor = StuColors.Primary,
                contentColor = StuColors.White,
                disabledContainerColor = StuColors.Gray400,
                disabledContentColor = StuColors.White,
            ),
        contentPadding = contentPadding,
    ) {
        Text(
            text = content,
            style = MaterialTheme.typography.labelLarge,
        )
    }
}

@Composable
@Preview
private fun PrimaryTextButtonPreview() {
    AppTheme {
        PrimaryTextButton(
            content = "예약 승인",
            onClick = {},
            enabled = false,
        )
    }
}
