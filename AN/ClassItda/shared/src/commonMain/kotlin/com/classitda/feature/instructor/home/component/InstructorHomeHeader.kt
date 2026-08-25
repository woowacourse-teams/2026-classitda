package com.classitda.feature.instructor.home.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import classitda.shared.generated.resources.Res
import classitda.shared.generated.resources.ic_expand_more
import com.classitda.core.designsystem.AppSpacing
import com.classitda.core.designsystem.AppTheme
import com.classitda.core.designsystem.InsColors
import com.classitda.core.designsystem.ThemeType
import org.jetbrains.compose.resources.painterResource

@Composable
internal fun InstructorHomeHeader(
    studioName: String,
    onStudioClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth().padding(horizontal = AppSpacing.screenPadding, vertical = AppSpacing.lg),
        verticalAlignment = Alignment.Top,
    ) {
        Column(Modifier.weight(1f)) {
            Text("안녕하세요, 이지은 강사님", color = InsColors.TextSecondary, style = MaterialTheme.typography.bodyMedium)
            Spacer(Modifier.height(AppSpacing.xs))
            Row(
                modifier = Modifier.clickable(onClick = onStudioClick),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(studioName, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Spacer(Modifier.width(AppSpacing.xs))
                Icon(
                    painter = painterResource(Res.drawable.ic_expand_more),
                    contentDescription = "시설 선택",
                    tint = InsColors.TextPrimary,
                    modifier = Modifier.size(20.dp),
                )
            }
        }
    }
}

@Preview(name = "강사 홈 헤더", showBackground = true, widthDp = 390)
@Composable
private fun InstructorHomeHeaderPreview() {
    AppTheme(theme = ThemeType.INSTRUCTOR) {
        InstructorHomeHeader(
            studioName = "클래스잇다 요가&필라테스",
            onStudioClick = {},
        )
    }
}
