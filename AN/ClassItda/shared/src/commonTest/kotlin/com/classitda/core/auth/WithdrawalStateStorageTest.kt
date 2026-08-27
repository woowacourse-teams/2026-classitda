package com.classitda.core.auth

import com.classitda.domain.model.auth.signup.LoginTokens
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class WithdrawalStateStorageTest {
    @Test
    fun `pending 상태를 인증 토큰과 독립적으로 유지한다`() {
        val tokenStorage = InMemoryAuthTokenStorage()
        val withdrawalStateStorage = InMemoryWithdrawalStateStorage()
        tokenStorage.write(
            LoginTokens(
                accessToken = "access-token",
                accessTokenExpiresInSeconds = 3600,
                refreshToken = "refresh-token",
                refreshTokenExpiresInSeconds = 86400,
            ),
        )

        assertFalse(withdrawalStateStorage.isPending())
        withdrawalStateStorage.markPending()
        tokenStorage.clear()

        assertTrue(withdrawalStateStorage.isPending())
    }
}
