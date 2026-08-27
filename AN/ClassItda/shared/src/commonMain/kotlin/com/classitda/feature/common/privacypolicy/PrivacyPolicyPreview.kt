package com.classitda.feature.common.privacypolicy

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.classitda.core.designsystem.AppSpacing
import com.classitda.core.designsystem.AppTheme
import com.classitda.core.designsystem.ThemeType

@Preview(name = "Privacy policy content", showBackground = true)
@Composable
private fun PrivacyPolicyContentPreview() {
    PrivacyPolicyPreview(theme = ThemeType.STUDENT, uiState = PrivacyPolicyUiState.Content())
}

@Preview(name = "Privacy policy loading", showBackground = true)
@Composable
private fun PrivacyPolicyLoadingPreview() {
    PrivacyPolicyPreview(theme = ThemeType.STUDENT, uiState = PrivacyPolicyUiState.Loading)
}

@Preview(name = "Privacy policy network error", showBackground = true)
@Composable
private fun PrivacyPolicyNetworkErrorPreview() {
    PrivacyPolicyPreview(
        theme = ThemeType.STUDENT,
        uiState = PrivacyPolicyUiState.Error(PrivacyPolicyError.NETWORK),
    )
}

@Preview(name = "Privacy policy TLS error", showBackground = true)
@Composable
private fun PrivacyPolicyTlsErrorPreview() {
    PrivacyPolicyPreview(
        theme = ThemeType.STUDENT,
        uiState = PrivacyPolicyUiState.Error(PrivacyPolicyError.TLS),
    )
}

@Preview(name = "Privacy policy invalid URL", showBackground = true)
@Composable
private fun PrivacyPolicyInvalidUrlPreview() {
    PrivacyPolicyPreview(
        theme = ThemeType.STUDENT,
        uiState = PrivacyPolicyUiState.Error(PrivacyPolicyError.INVALID_INITIAL_URL),
    )
}

@Preview(name = "Privacy policy blocked navigation", showBackground = true)
@Composable
private fun PrivacyPolicyBlockedNavigationPreview() {
    PrivacyPolicyPreview(
        theme = ThemeType.STUDENT,
        uiState = PrivacyPolicyUiState.Content(isBlockedNavigationNoticeVisible = true),
    )
}

@Preview(name = "Privacy policy instructor", showBackground = true)
@Composable
private fun PrivacyPolicyInstructorPreview() {
    PrivacyPolicyPreview(theme = ThemeType.INSTRUCTOR, uiState = PrivacyPolicyUiState.Content())
}

@Preview(
    name = "Privacy policy small screen",
    widthDp = 320,
    heightDp = 568,
    showBackground = true,
)
@Composable
private fun PrivacyPolicySmallScreenPreview() {
    PrivacyPolicyPreview(
        theme = ThemeType.STUDENT,
        uiState = PrivacyPolicyUiState.Content(isBlockedNavigationNoticeVisible = true),
    )
}

@Preview(
    name = "Privacy policy large text",
    widthDp = 411,
    heightDp = 891,
    fontScale = 1.5f,
    showBackground = true,
)
@Composable
private fun PrivacyPolicyLargeTextPreview() {
    PrivacyPolicyPreview(
        theme = ThemeType.STUDENT,
        uiState = PrivacyPolicyUiState.Error(PrivacyPolicyError.UNKNOWN),
    )
}

@Composable
private fun PrivacyPolicyPreview(
    theme: ThemeType,
    uiState: PrivacyPolicyUiState,
) {
    AppTheme(theme = theme) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background,
        ) {
            PrivacyPolicyScreen(
                uiState = uiState,
                onAction = {},
                content = {
                    PrivacyPolicyPreviewDocument(modifier = Modifier.fillMaxSize())
                },
            )
        }
    }
}

@Composable
private fun PrivacyPolicyPreviewDocument(modifier: Modifier = Modifier) {
    Column(
        modifier =
            modifier
                .padding(AppSpacing.screenPadding),
        verticalArrangement = Arrangement.spacedBy(AppSpacing.md),
    ) {
        Text(
            text = "정책 문서 미리보기",
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onBackground,
        )
        Text(
            text = "플랫폼 WebView가 이 영역에 표시됩니다.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        repeat(5) {
            Text(
                text = "개인정보처리방침 내용 예시입니다.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onBackground,
            )
        }
    }
}
