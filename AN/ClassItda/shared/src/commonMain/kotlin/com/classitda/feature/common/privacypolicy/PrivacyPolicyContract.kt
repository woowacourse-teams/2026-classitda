package com.classitda.feature.common.privacypolicy

sealed interface PrivacyPolicyUiState {
    data object Loading : PrivacyPolicyUiState

    data class Content(
        val isBlockedNavigationNoticeVisible: Boolean = false,
    ) : PrivacyPolicyUiState

    data class Error(
        val reason: PrivacyPolicyError,
    ) : PrivacyPolicyUiState
}

enum class PrivacyPolicyError {
    NETWORK,
    TLS,
    INVALID_INITIAL_URL,
    UNKNOWN,
}

sealed interface PrivacyPolicyAction {
    data object Back : PrivacyPolicyAction

    data object Retry : PrivacyPolicyAction

    data object DismissBlockedNavigationNotice : PrivacyPolicyAction
}
