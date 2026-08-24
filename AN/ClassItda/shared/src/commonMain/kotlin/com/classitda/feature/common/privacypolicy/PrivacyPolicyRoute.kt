package com.classitda.feature.common.privacypolicy

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier

@Composable
internal fun PrivacyPolicyRoute(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var uiState by remember { mutableStateOf<PrivacyPolicyUiState>(PrivacyPolicyUiState.Loading) }
    var reloadToken by remember { mutableIntStateOf(0) }

    PrivacyPolicyScreen(
        uiState = uiState,
        onAction = { action ->
            when (action) {
                PrivacyPolicyAction.Back -> {
                    onBack()
                }

                PrivacyPolicyAction.Retry -> {
                    uiState = PrivacyPolicyUiState.Loading
                    reloadToken += 1
                }

                PrivacyPolicyAction.DismissBlockedNavigationNotice -> {
                    val contentState = uiState as? PrivacyPolicyUiState.Content
                    if (contentState != null) {
                        uiState = contentState.copy(isBlockedNavigationNoticeVisible = false)
                    }
                }
            }
        },
        modifier = modifier,
        content = {
            PrivacyPolicyWebContent(
                reloadToken = reloadToken,
                onLoadingChanged = { isLoading ->
                    if (isLoading) {
                        uiState = PrivacyPolicyUiState.Loading
                    } else if (uiState is PrivacyPolicyUiState.Loading) {
                        uiState = PrivacyPolicyUiState.Content()
                    }
                },
                onLoadFailed = { error ->
                    uiState = PrivacyPolicyUiState.Error(error)
                },
                onNavigationBlocked = {
                    uiState =
                        PrivacyPolicyUiState.Content(
                            isBlockedNavigationNoticeVisible = true,
                        )
                },
                modifier = Modifier.fillMaxSize(),
            )
        },
    )
}
