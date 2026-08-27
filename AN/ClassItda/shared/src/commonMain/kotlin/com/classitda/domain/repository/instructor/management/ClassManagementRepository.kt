package com.classitda.domain.repository.instructor.management

import com.classitda.domain.model.instructor.management.ClassSession
import com.classitda.domain.model.instructor.management.ClassSessionCreateRequest
import com.classitda.domain.model.instructor.management.ClassSessionMember

interface ClassManagementRepository {
    suspend fun getSessions(): List<ClassSession>

    suspend fun getCustomCategories(): List<String>

    suspend fun createSession(request: ClassSessionCreateRequest)

    suspend fun updateSession(session: ClassSession): ClassSession

    suspend fun deleteSession(id: String)

    suspend fun updateSessionMembers(
        sessionId: String,
        members: List<ClassSessionMember>,
    )
}
