package com.classitda.core.auth

import platform.Foundation.NSUserDefaults

class IosWithdrawalStateStorage(
    private val userDefaults: NSUserDefaults = NSUserDefaults.standardUserDefaults,
) : WithdrawalStateStorage {
    override fun isPending(): Boolean = userDefaults.boolForKey(PENDING_KEY)

    override fun markPending() {
        userDefaults.setBool(true, forKey = PENDING_KEY)
    }

    private companion object {
        const val PENDING_KEY = "withdrawal_pending"
    }
}
