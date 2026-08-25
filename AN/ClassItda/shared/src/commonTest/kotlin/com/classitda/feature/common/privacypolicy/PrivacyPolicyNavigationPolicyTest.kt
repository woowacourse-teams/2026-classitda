package com.classitda.feature.common.privacypolicy

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class PrivacyPolicyNavigationPolicyTest {
    private val policy = PrivacyPolicyConfig.navigationPolicy

    @Test
    fun `고정된 개인정보처리방침 URL은 허용한다`() {
        assertTrue(policy.allows(target()))
    }

    @Test
    fun `scheme과 host의 대소문자 차이는 parser 정규화 결과로 허용한다`() {
        assertTrue(
            policy.allows(
                target(
                    scheme = "HTTPS",
                    host = "CLASSITDA.COM",
                ),
            ),
        )
    }

    @Test
    fun `명시하지 않은 port와 443 port만 허용한다`() {
        assertTrue(policy.allows(target(port = null)))
        assertTrue(policy.allows(target(port = 443)))
        assertFalse(policy.allows(target(port = 8443)))
    }

    @Test
    fun `같은 문서 path는 허용한다`() {
        assertTrue(policy.allows(target(path = "/privacy-policy")))
    }

    @Test
    fun `비승인 path와 query는 차단한다`() {
        assertFalse(policy.allows(target(path = "/")))
        assertFalse(policy.allows(target(path = "/privacy-policy/")))
        assertFalse(policy.allows(target(path = "/privacy")))
        assertFalse(policy.allows(target(query = "next=https://naver.com")))
    }

    @Test
    fun `host 위장과 userInfo를 차단한다`() {
        assertFalse(policy.allows(target(host = "classitda.com.evil.com")))
        assertFalse(policy.allows(target(host = "evil-classitda.com")))
        assertFalse(policy.allows(target(host = "www.classitda.com")))
        assertFalse(policy.allows(target(hasUserInfo = true)))
    }

    @Test
    fun `외부 host와 userInfo 기반 approved host 위장을 차단한다`() {
        assertFalse(policy.allows(target(host = "naver.com")))
        assertFalse(policy.allows(target(host = "google.com")))
        assertFalse(policy.allows(target(host = "classitda.com.evil.com")))
        assertFalse(policy.allows(target(host = "evil-classitda.com")))
        assertFalse(policy.allows(target(host = "classitda.com", hasUserInfo = true)))
    }

    @Test
    fun `HTTP downgrade와 위험 scheme을 차단한다`() {
        assertFalse(policy.allows(target(scheme = "http")))
        assertFalse(policy.allows(target(scheme = "javascript", host = null, path = "")))
        assertFalse(policy.allows(target(scheme = "intent", host = null, path = "")))
        assertFalse(policy.allows(target(scheme = "file", host = null, path = "/etc/passwd")))
        assertFalse(policy.allows(target(scheme = "content", host = null, path = "provider/item")))
        assertFalse(policy.allows(target(scheme = "data", host = null, path = "text/html,blocked")))
        assertFalse(policy.allows(target(scheme = "tel", host = null, path = "+821012345678")))
        assertFalse(policy.allows(target(scheme = "mailto", host = null, path = "support@classitda.com")))
    }

    @Test
    fun `malformed target은 차단한다`() {
        assertFalse(
            policy.allows(
                PrivacyPolicyNavigationTarget(
                    scheme = null,
                    host = null,
                    port = null,
                    hasUserInfo = false,
                    path = "",
                    query = null,
                ),
            ),
        )
    }

    @Test
    fun `승인된 query 문자열만 별도 정책에서 허용할 수 있다`() {
        val queryPolicy = policy.copy(allowedQueryStrings = setOf("lang=ko"))

        assertTrue(queryPolicy.allows(target(query = "lang=ko")))
        assertFalse(queryPolicy.allows(target(query = "lang=en")))
        assertFalse(queryPolicy.allows(target(query = "lang=ko&next=https://naver.com")))
    }

    private fun target(
        scheme: String? = "https",
        host: String? = "classitda.com",
        port: Int? = null,
        hasUserInfo: Boolean = false,
        path: String = "/privacy-policy",
        query: String? = null,
    ) = PrivacyPolicyNavigationTarget(
        scheme = scheme,
        host = host,
        port = port,
        hasUserInfo = hasUserInfo,
        path = path,
        query = query,
    )
}
