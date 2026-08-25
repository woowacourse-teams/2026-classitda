package com.classitda.data.repository.instructor.management

import co.touchlab.kermit.Logger
import com.classitda.core.network.toErrorResponse
import com.classitda.data.remote.api.ClassTemplatesApi
import com.classitda.data.remote.api.ClassTypesApi
import com.classitda.domain.model.instructor.management.ClassTemplate
import com.classitda.domain.model.instructor.management.ClassType
import com.classitda.domain.repository.instructor.management.ClassTemplateManagementException
import com.classitda.domain.repository.instructor.management.ClassTemplateManagementRepository
import io.ktor.client.plugins.ResponseException
import io.ktor.http.HttpStatusCode

internal class ClassTemplateManagementRepositoryImpl(
    private val classTemplatesApi: ClassTemplatesApi,
    private val classTypesApi: ClassTypesApi,
) : ClassTemplateManagementRepository {
    override suspend fun getTemplates(studioId: String): List<ClassTemplate> =
        handlingApiErrors {
            classTemplatesApi.getClassTemplates(studioId.toStudioId()).map { it.toDomain() }
        }

    override suspend fun getTemplate(
        studioId: String,
        id: String,
    ): ClassTemplate? =
        handlingApiErrors {
            classTemplatesApi.getClassTemplates(studioId.toStudioId()).map { it.toDomain() }.find { it.id == id }
        }

    // 201 Created만 내려주고 서버가 생성된 값을 돌려주지 않는다. 요청이 성공했다면
    // 서버 상태가 요청 그대로 반영됐다고 보고, 입력값을 그대로 돌려준다.
    override suspend fun createTemplate(
        studioId: String,
        template: ClassTemplate,
    ): ClassTemplate {
        Logger.d("createTemplate template: $template")
        return handlingApiErrors {
            classTemplatesApi.createClassTemplate(studioId.toStudioId(), template.toCreateRequestDto())
            template
        }
    }

    // PUT은 204 No Content라 서버가 갱신된 값을 돌려주지 않는다. 요청이 성공했다면
    // 서버 상태가 요청 그대로 반영됐다고 보고, 입력값을 그대로 돌려준다.
    override suspend fun updateTemplate(
        studioId: String,
        template: ClassTemplate,
    ): ClassTemplate {
        Logger.d("updateTemplate template: $template")
        return handlingApiErrors {
            classTemplatesApi.editClassTemplate(
                studioId.toStudioId(),
                template.id.toClassTemplateId(),
                template.toUpdateRequestDto(),
            )
            template
        }
    }

    override suspend fun deleteTemplate(
        studioId: String,
        id: String,
    ) {
        handlingApiErrors {
            classTemplatesApi.deleteClassTemplate(studioId.toStudioId(), id.toClassTemplateId())
        }
    }

    override suspend fun getClassTypes(studioId: String): List<ClassType> =
        handlingApiErrors {
            classTypesApi.getClassTypes(studioId.toStudioId()).map { it.toDomain() }
        }
}

private suspend fun <T> handlingApiErrors(block: suspend () -> T): T =
    try {
        block()
    } catch (e: ResponseException) {
        throw e.toClassTemplateManagementException()
    }

private suspend fun ResponseException.toClassTemplateManagementException(): ClassTemplateManagementException {
    val error = toErrorResponse()
    return when (response.status) {
        HttpStatusCode.BadRequest -> ClassTemplateManagementException.InvalidRequest(error.code, error.message)
        HttpStatusCode.Unauthorized -> ClassTemplateManagementException.Unauthorized(error.code, error.message)
        HttpStatusCode.Forbidden -> ClassTemplateManagementException.Forbidden(error.code, error.message)
        HttpStatusCode.NotFound -> ClassTemplateManagementException.NotFound(error.code, error.message)
        HttpStatusCode.Conflict -> ClassTemplateManagementException.Conflict(error.code, error.message)
        else -> ClassTemplateManagementException.Unknown(error.code, error.message)
    }
}
