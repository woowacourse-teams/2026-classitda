package com.classitda.core.auth

import com.classitda.domain.model.auth.signup.LoginTokens
import com.russhwolf.settings.Settings

/**
 * 앱 세션 토큰의 저장·삭제를 담당한다.
 *
 * 구현체를 주입해 플랫폼별 보안 저장소로 교체할 수 있도록 인증 도메인과
 * 저장 방식의 결합을 피한다.
 */
interface AuthTokenStorage {
    fun read(): LoginTokens?

    fun write(tokens: LoginTokens)

    fun clear()
}

/** Clears persisted, user-scoped data when the authenticated session changes. */
fun interface SessionCacheCleaner {
    suspend fun clear()
}

/**
 * 현재 앱의 기본 저장소.
 *
 * 토큰을 소스 코드나 로그에 남기지 않고 플랫폼 설정 저장소에 보관한다.
 * 배포 전에는 Android Keystore/iOS Keychain 구현체를 주입하는 것이 권장된다.
 */
class SettingsAuthTokenStorage(
    private val settings: Settings,
) : AuthTokenStorage {
    override fun read(): LoginTokens? {
        val accessToken = settings.getStringOrNull(ACCESS_TOKEN_KEY) ?: return null
        val refreshToken = settings.getStringOrNull(REFRESH_TOKEN_KEY) ?: return null
        val accessTokenExpiresIn = settings.getLongOrNull(ACCESS_TOKEN_EXPIRES_KEY) ?: return null
        val refreshTokenExpiresIn = settings.getLongOrNull(REFRESH_TOKEN_EXPIRES_KEY) ?: return null

        return runCatching {
            LoginTokens(
                accessToken = accessToken,
                accessTokenExpiresInSeconds = accessTokenExpiresIn,
                refreshToken = refreshToken,
                refreshTokenExpiresInSeconds = refreshTokenExpiresIn,
            )
        }.getOrNull()
    }

    override fun write(tokens: LoginTokens) {
        settings.putString(ACCESS_TOKEN_KEY, tokens.accessToken)
        settings.putLong(ACCESS_TOKEN_EXPIRES_KEY, tokens.accessTokenExpiresInSeconds)
        settings.putString(REFRESH_TOKEN_KEY, tokens.refreshToken)
        settings.putLong(REFRESH_TOKEN_EXPIRES_KEY, tokens.refreshTokenExpiresInSeconds)
    }

    override fun clear() {
        settings.remove(ACCESS_TOKEN_KEY)
        settings.remove(ACCESS_TOKEN_EXPIRES_KEY)
        settings.remove(REFRESH_TOKEN_KEY)
        settings.remove(REFRESH_TOKEN_EXPIRES_KEY)
    }

    private companion object {
        const val ACCESS_TOKEN_KEY = "auth.access_token"
        const val ACCESS_TOKEN_EXPIRES_KEY = "auth.access_token_expires_in"
        const val REFRESH_TOKEN_KEY = "auth.refresh_token"
        const val REFRESH_TOKEN_EXPIRES_KEY = "auth.refresh_token_expires_in"
    }
}

class InMemoryAuthTokenStorage : AuthTokenStorage {
    private var tokens: LoginTokens? = null

    override fun read(): LoginTokens? = tokens

    override fun write(tokens: LoginTokens) {
        this.tokens = tokens
    }

    override fun clear() {
        tokens = null
    }
}
