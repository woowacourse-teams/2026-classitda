package com.classitda.domain.repository.instructor.management

import com.classitda.domain.model.instructor.management.ClassTemplate
import com.classitda.domain.model.instructor.management.ClassType

interface ClassTemplateManagementRepository {
    suspend fun getTemplates(studioId: String): List<ClassTemplate>

    suspend fun createTemplate(
        studioId: String,
        template: ClassTemplate,
    ): ClassTemplate

    suspend fun updateTemplate(
        studioId: String,
        template: ClassTemplate,
    ): ClassTemplate

    suspend fun deleteTemplate(
        studioId: String,
        id: String,
    )

    suspend fun getClassTypes(studioId: String): List<ClassType>
}
