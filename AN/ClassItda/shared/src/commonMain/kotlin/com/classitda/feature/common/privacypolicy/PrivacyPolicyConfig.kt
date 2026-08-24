package com.classitda.feature.common.privacypolicy

internal object PrivacyPolicyConfig {
    const val INITIAL_URL = "https://classitda.com/privacy-policy"

    val navigationPolicy =
        PrivacyPolicyNavigationPolicy(
            allowedScheme = "https",
            allowedHost = "classitda.com",
            allowedPaths = setOf("/privacy-policy"),
            allowedQueryStrings = emptySet(),
        )
}
