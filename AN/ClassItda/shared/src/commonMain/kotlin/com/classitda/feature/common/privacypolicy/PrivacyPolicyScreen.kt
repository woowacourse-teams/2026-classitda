package com.classitda.feature.common.privacypolicy

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import classitda.shared.generated.resources.Res
import classitda.shared.generated.resources.my_page_privacy_policy
import classitda.shared.generated.resources.privacy_policy_error_invalid_initial_url
import classitda.shared.generated.resources.privacy_policy_error_network
import classitda.shared.generated.resources.privacy_policy_error_title
import classitda.shared.generated.resources.privacy_policy_error_tls
import classitda.shared.generated.resources.privacy_policy_error_unknown
import classitda.shared.generated.resources.privacy_policy_loading
import classitda.shared.generated.resources.privacy_policy_navigation_blocked
import classitda.shared.generated.resources.privacy_policy_navigation_blocked_dismiss
import classitda.shared.generated.resources.privacy_policy_retry
import com.classitda.core.designsystem.AppShape
import com.classitda.core.designsystem.AppSpacing
import com.classitda.core.designsystem.component.NavigateBackTopBar
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun PrivacyPolicyScreen(
    uiState: PrivacyPolicyUiState,
    onAction: (PrivacyPolicyAction) -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit,
) {
    Column(modifier = modifier.fillMaxSize()) {
        NavigateBackTopBar(
            onNavigateBack = { onAction(PrivacyPolicyAction.Back) },
            title = stringResource(Res.string.my_page_privacy_policy),
            modifier = Modifier.semantics { heading() },
        )

        Box(modifier = Modifier.fillMaxSize()) {
            content()

            when (uiState) {
                PrivacyPolicyUiState.Loading -> {
                    PrivacyPolicyLoading(
                        modifier = Modifier.background(MaterialTheme.colorScheme.background),
                    )
                }

                is PrivacyPolicyUiState.Error -> {
                    PrivacyPolicyErrorContent(
                        error = uiState.reason,
                        onRetry = { onAction(PrivacyPolicyAction.Retry) },
                        modifier = Modifier.background(MaterialTheme.colorScheme.background),
                    )
                }

                is PrivacyPolicyUiState.Content -> {
                    if (uiState.isBlockedNavigationNoticeVisible) {
                        PrivacyPolicyBlockedNavigationNotice(
                            onDismiss = {
                                onAction(PrivacyPolicyAction.DismissBlockedNavigationNotice)
                            },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PrivacyPolicyLoading(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.fillMaxSize().semantics { liveRegion = LiveRegionMode.Polite },
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            Text(
                text = stringResource(Res.string.privacy_policy_loading),
                modifier = Modifier.padding(top = AppSpacing.md),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onBackground,
            )
        }
    }
}

@Composable
private fun PrivacyPolicyErrorContent(
    error: PrivacyPolicyError,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier =
            modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(AppSpacing.screenPadding)
                .semantics { liveRegion = LiveRegionMode.Assertive },
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = stringResource(Res.string.privacy_policy_error_title),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center,
        )
        Text(
            text = errorMessage(error),
            modifier = Modifier.padding(top = AppSpacing.sm),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
        TextButton(
            onClick = onRetry,
            modifier = Modifier.padding(top = AppSpacing.sm),
        ) {
            Text(text = stringResource(Res.string.privacy_policy_retry))
        }
    }
}

@Composable
private fun BoxScope.PrivacyPolicyBlockedNavigationNotice(
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier =
            modifier
                .align(Alignment.TopCenter)
                .fillMaxWidth()
                .padding(AppSpacing.md)
                .semantics { liveRegion = LiveRegionMode.Assertive },
        shape = AppShape.Card,
        color = MaterialTheme.colorScheme.errorContainer,
        contentColor = MaterialTheme.colorScheme.onErrorContainer,
    ) {
        Row(
            modifier = Modifier.padding(start = AppSpacing.md, end = AppSpacing.xs),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = stringResource(Res.string.privacy_policy_navigation_blocked),
                modifier = Modifier.weight(1f).padding(vertical = AppSpacing.md),
                style = MaterialTheme.typography.bodyMedium,
            )
            TextButton(onClick = onDismiss) {
                Text(
                    text =
                        stringResource(
                            Res.string.privacy_policy_navigation_blocked_dismiss,
                        ),
                )
            }
        }
    }
}

@Composable
private fun errorMessage(error: PrivacyPolicyError): String =
    when (error) {
        PrivacyPolicyError.NETWORK -> {
            stringResource(Res.string.privacy_policy_error_network)
        }

        PrivacyPolicyError.TLS -> {
            stringResource(Res.string.privacy_policy_error_tls)
        }

        PrivacyPolicyError.INVALID_INITIAL_URL -> {
            stringResource(Res.string.privacy_policy_error_invalid_initial_url)
        }

        PrivacyPolicyError.UNKNOWN -> {
            stringResource(Res.string.privacy_policy_error_unknown)
        }
    }
