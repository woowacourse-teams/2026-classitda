package com.classitda.data.local.instructor.mypage

import com.classitda.core.auth.SessionCacheCleaner
import com.classitda.domain.model.instructor.mypage.InstructorMyPageSummary

internal interface InstructorMyPageCache {
    suspend fun read(): InstructorMyPageSummary?

    suspend fun replace(summary: InstructorMyPageSummary)

    suspend fun clear()
}

internal class RoomInstructorMyPageCache(
    private val dao: InstructorMyPageCacheDao,
) : InstructorMyPageCache,
    SessionCacheCleaner {
    override suspend fun read(): InstructorMyPageSummary? =
        dao.get()?.let { InstructorMyPageSummary(name = it.name, phoneNumber = it.phoneNumber) }

    override suspend fun replace(summary: InstructorMyPageSummary) {
        dao.upsert(
            InstructorMyPageCacheEntity(
                name = summary.name,
                phoneNumber = summary.phoneNumber,
            ),
        )
    }

    override suspend fun clear() = dao.clear()
}
