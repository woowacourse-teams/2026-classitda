package com.classitda.data.repository.instructor.management

import com.classitda.core.studio.InstructorStudioContext
import com.classitda.data.remote.api.ClassSessionsApi
import com.classitda.data.remote.api.ClassTypesApi
import com.classitda.domain.model.instructor.management.ClassSession
import com.classitda.domain.model.instructor.management.ClassSessionCreateRequest
import com.classitda.domain.model.instructor.management.ClassSessionMember
import com.classitda.domain.repository.instructor.management.ClassManagementRepository

private const val CLASS_SESSIONS_PAGE_SIZE = 100

internal class ClassManagementRepositoryImpl(
    private val classSessionsApi: ClassSessionsApi,
    private val classTypesApi: ClassTypesApi,
    private val studioContext: InstructorStudioContext,
) : ClassManagementRepository {
    private suspend fun resolveStudioId(): Long =
        studioContext
            .getSelectedStudio()
            .id.value
            .toStudioId()

    override suspend fun getSessions(): List<ClassSession> =
        handlingApiErrors {
            val studioId = resolveStudioId()
            val sessions = mutableListOf<ClassSession>()
            var cursor: String? = null
            do {
                val page =
                    classSessionsApi.getInstructorClassSessions(
                        studioId,
                        cursor,
                        CLASS_SESSIONS_PAGE_SIZE,
                    )
                sessions += page.items.map { it.toDomain() }
                cursor = page.nextCursor
            } while (page.hasNext)
            sessions
        }

    override suspend fun getCustomCategories(): List<String> =
        handlingApiErrors {
            classTypesApi.getClassTypes(resolveStudioId()).map { it.name }
        }

    override suspend fun createSession(request: ClassSessionCreateRequest) {
        handlingApiErrors {
            classSessionsApi.createClassSession(resolveStudioId(), request.toRequestDto())
        }
    }

    override suspend fun updateSession(session: ClassSession): ClassSession =
        handlingApiErrors {
            classSessionsApi.updateClassSession(
                resolveStudioId(),
                session.id.toClassSessionId(),
                session.toUpdateRequestDto(),
            )
            session
        }

    override suspend fun deleteSession(id: String) {
        handlingApiErrors {
            classSessionsApi.cancelClassSession(resolveStudioId(), id.toClassSessionId())
        }
    }

    // 실제 API는 예약자 명단을 한 번에 교체하는 엔드포인트를 제공하지 않고 개별 예약 등록/취소만 지원한다.
    // TODO: 대리 예약 등록/취소 API(POST|DELETE .../enrollments)로 교체.
    override suspend fun updateSessionMembers(
        sessionId: String,
        members: List<ClassSessionMember>,
    ) {
        error("예약자 명단 일괄 수정은 아직 실제 API와 연동되지 않았습니다.")
    }
}
