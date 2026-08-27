package com.classitda.feature.instructor.management

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import classitda.shared.generated.resources.Res
import classitda.shared.generated.resources.ic_arrow_forward
import com.classitda.core.designsystem.AppShape
import com.classitda.core.designsystem.AppSpacing
import com.classitda.core.designsystem.AppTheme
import com.classitda.core.designsystem.InsColors
import com.classitda.core.designsystem.ThemeType
import org.jetbrains.compose.resources.painterResource

@Composable
internal fun ManagementMenuScreen(
    onClassListClick: () -> Unit,
    onClassTemplateManagementClick: () -> Unit,
    onMemberManagementClick: () -> Unit,
    bottomBar: @Composable () -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier,
        containerColor = InsColors.Background,
        bottomBar = bottomBar,
    ) { contentPadding ->
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(contentPadding)
                    .padding(AppSpacing.screenPadding),
        ) {
            Text(
                text = "관리",
                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                color = InsColors.TextPrimary,
            )
            Text(
                text = "관리할 항목을 선택해 주세요",
                style = MaterialTheme.typography.bodyMedium,
                color = InsColors.TextSecondary,
                modifier = Modifier.padding(top = AppSpacing.xs, bottom = AppSpacing.lg),
            )

            Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.sm)) {
                ManagementMenuItem(title = "수업 목록", onClick = onClassListClick)
                ManagementMenuItem(title = "수업 템플릿 관리", onClick = onClassTemplateManagementClick)
                ManagementMenuItem(title = "회원 관리", onClick = onMemberManagementClick)
            }
        }
    }
}

@Composable
private fun ManagementMenuItem(
    title: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.fillMaxWidth().clickable(onClick = onClick),
        shape = AppShape.Card,
        color = InsColors.Surface,
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(AppSpacing.cardPadding),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = InsColors.TextPrimary,
            )
            Icon(
                painter = painterResource(Res.drawable.ic_arrow_forward),
                contentDescription = null,
                tint = InsColors.TextSecondary,
            )
        }
    }
}

@Composable
@Preview
private fun ManagementMenuScreenPreview() {
    AppTheme(theme = ThemeType.INSTRUCTOR) {
        ManagementMenuScreen(
            onClassListClick = {},
            onClassTemplateManagementClick = {},
            onMemberManagementClick = {},
            bottomBar = {},
        )
    }
}
