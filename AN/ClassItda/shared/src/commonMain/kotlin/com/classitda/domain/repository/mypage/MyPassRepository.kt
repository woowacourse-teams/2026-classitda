package com.classitda.domain.repository.mypage

import com.classitda.domain.model.mypage.MyPass
import com.classitda.domain.model.mypage.MyPassHistoryEntry
import com.classitda.domain.model.mypage.MyPassHoldingReceipt
import kotlinx.datetime.LocalDate

interface MyPassRepository {
    suspend fun getMyPasses(): List<MyPass>

    suspend fun getMyPass(passId: String): MyPass?

    suspend fun getMyPassHistory(passId: String): List<MyPassHistoryEntry>

    suspend fun requestHolding(
        passId: String,
        startDate: LocalDate,
        endDate: LocalDate,
    ): MyPassHoldingReceipt
}
