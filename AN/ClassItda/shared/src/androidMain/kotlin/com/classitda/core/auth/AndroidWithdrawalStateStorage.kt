package com.classitda.core.auth

import android.content.Context

class AndroidWithdrawalStateStorage(
    context: Context,
) : WithdrawalStateStorage {
    private val preferences = context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)

    override fun isPending(): Boolean = preferences.getBoolean(PENDING_KEY, false)

    override fun markPending() {
        preferences.edit().putBoolean(PENDING_KEY, true).apply()
    }

    private companion object {
        const val PREFERENCES_NAME = "account_lifecycle"
        const val PENDING_KEY = "withdrawal_pending"
    }
}
