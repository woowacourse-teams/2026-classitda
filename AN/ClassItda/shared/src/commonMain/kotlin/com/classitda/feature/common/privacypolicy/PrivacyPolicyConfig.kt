package com.classitda.feature.common.privacypolicy

internal object PrivacyPolicyConfig {
    const val INITIAL_URL = "https://www.classitda.com/privacy"

    val navigationPolicy =
        PrivacyPolicyNavigationPolicy(
            allowedScheme = "https",
            allowedHost = "www.classitda.com",
            allowedPaths = setOf("/privacy"),
            allowedQueryStrings = emptySet(),
        )
}
