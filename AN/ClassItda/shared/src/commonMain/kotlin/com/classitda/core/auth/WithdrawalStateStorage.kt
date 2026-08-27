package com.classitda.core.auth

interface WithdrawalStateStorage {
    fun isPending(): Boolean

    fun markPending()
}

class InMemoryWithdrawalStateStorage : WithdrawalStateStorage {
    private var pending = false

    override fun isPending(): Boolean = pending

    override fun markPending() {
        pending = true
    }
}
