package com.classitda.domain.repository.instructor.management

import com.classitda.domain.model.instructor.management.ClassSession
import com.classitda.domain.model.instructor.management.ClassSessionMember
import com.classitda.domain.model.instructor.management.ClassTemplate

interface ClassManagementRepository {
    suspend fun getTemplates(): List<ClassTemplate>

    suspend fun getTemplate(id: String): ClassTemplate?

    suspend fun getSessions(): List<ClassSession>

    suspend fun getCustomCategories(): List<String>

    suspend fun createTemplate(template: ClassTemplate): ClassTemplate

    suspend fun updateTemplate(template: ClassTemplate): ClassTemplate

    suspend fun deleteTemplate(id: String)

    suspend fun createSession(session: ClassSession): ClassSession

    suspend fun updateSession(session: ClassSession): ClassSession

    suspend fun deleteSession(id: String)

    suspend fun updateSessionMembers(
        sessionId: String,
        members: List<ClassSessionMember>,
    )
}
