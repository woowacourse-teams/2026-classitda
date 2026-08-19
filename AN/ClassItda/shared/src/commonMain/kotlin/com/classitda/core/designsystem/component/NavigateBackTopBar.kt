package com.classitda.core.designsystem.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import classitda.shared.generated.resources.Res
import classitda.shared.generated.resources.ic_arrow_back
import com.classitda.core.designsystem.AppTheme
import com.classitda.core.designsystem.StuColors
import org.jetbrains.compose.resources.painterResource

@Composable
fun NavigateBackTopBar(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    title: String? = null,
) {
    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .height(52.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            painter = painterResource(Res.drawable.ic_arrow_back),
            contentDescription = "뒤로가기",
            modifier = Modifier.size(48.dp).clickable(onClick = onNavigateBack).padding(12.dp),
            tint = StuColors.TextPrimary,
        )

        title?.let {
            Text(
                text = it,
                style = MaterialTheme.typography.titleMedium,
                color = StuColors.TextPrimary,
            )
        }
    }
}

@Composable
@Preview
private fun NavigateBackTopBarPreview() {
    AppTheme {
        NavigateBackTopBar(
            onNavigateBack = {},
            title = "내 수강권",
        )
    }
}
