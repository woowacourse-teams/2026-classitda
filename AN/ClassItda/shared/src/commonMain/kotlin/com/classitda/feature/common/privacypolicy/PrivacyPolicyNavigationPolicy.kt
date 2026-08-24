package com.classitda.feature.common.privacypolicy

internal data class PrivacyPolicyNavigationTarget(
    val scheme: String?,
    val host: String?,
    val port: Int?,
    val hasUserInfo: Boolean,
    val path: String,
    val query: String?,
)

internal data class PrivacyPolicyNavigationPolicy(
    val allowedScheme: String,
    val allowedHost: String,
    val allowedPaths: Set<String>,
    val allowedQueryStrings: Set<String>,
)

internal fun PrivacyPolicyNavigationPolicy.allows(target: PrivacyPolicyNavigationTarget): Boolean =
    target.scheme?.equals(allowedScheme, ignoreCase = true) == true &&
        target.host?.equals(allowedHost, ignoreCase = true) == true &&
        (target.port == null || target.port == 443) &&
        !target.hasUserInfo &&
        target.path in allowedPaths &&
        (target.query == null || target.query in allowedQueryStrings)
