package com.classitda.data.repository.instructor.management

import com.classitda.domain.model.instructor.management.ClassForm
import com.classitda.domain.model.instructor.management.ClassSession
import com.classitda.domain.model.instructor.management.ClassSessionMember
import com.classitda.domain.model.instructor.management.ClassSessionStatus
import com.classitda.domain.model.instructor.management.ClassTemplate
import com.classitda.domain.model.instructor.management.ClassTemplateSchedule
import com.classitda.domain.repository.instructor.management.ClassManagementRepository
import kotlinx.coroutines.delay
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import kotlin.time.Clock

// TODO: 실제 API 연동 시 remote 기반 구현으로 교체
internal class FakeClassManagementRepository : ClassManagementRepository {
    private val templates =
        mutableListOf(
            ClassTemplate(
                id = "1",
                tags = listOf("그룹 수업", "필라테스"),
                title = "리포머 밸런스",
                classForm = ClassForm.GROUP,
                durationMinutes = 50,
                capacity = 8,
                schedule =
                    ClassTemplateSchedule(
                        startTime = LocalTime(10, 0),
                        endTime = LocalTime(10, 50),
                        repeatDays = listOf(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY),
                    ),
            ),
            ClassTemplate(
                id = "2",
                tags = listOf("그룹 수업", "필라테스"),
                title = "리포머 밸런스",
                classForm = ClassForm.GROUP,
                durationMinutes = 50,
                capacity = 8,
                schedule =
                    ClassTemplateSchedule(
                        startTime = LocalTime(12, 0),
                        endTime = LocalTime(12, 50),
                        repeatDays = listOf(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY),
                    ),
            ),
            ClassTemplate(
                id = "3",
                tags = listOf("개인 수업", "요가"),
                title = "1:1 개인 수업",
                classForm = ClassForm.INDIVIDUAL,
                durationMinutes = 50,
                capacity = 1,
                schedule = null,
            ),
        )

    private val sessions =
        mutableListOf(
            ClassSession(
                id = "1",
                tags = listOf("그룹 수업", "필라테스"),
                title = "리포머 밸런스",
                startAt = LocalDateTime(2026, 8, 12, 19, 30),
                endAt = LocalDateTime(2026, 8, 12, 20, 20),
                reservedCount = 8,
                capacity = 10,
                status = ClassSessionStatus.SCHEDULED,
                members = demoMembers,
            ),
            ClassSession(
                id = "2",
                tags = listOf("그룹 수업", "요가"),
                title = "하타 요가",
                startAt = LocalDateTime(2026, 8, 9, 11, 0),
                endAt = LocalDateTime(2026, 8, 9, 11, 50),
                reservedCount = 8,
                capacity = 10,
                status = ClassSessionStatus.CANCELLED,
                members = demoMembers,
            ),
            ClassSession(
                id = "3",
                tags = listOf("개인 수업", "요가"),
                title = "리포머 밸런스",
                startAt = LocalDateTime(2026, 8, 8, 10, 0),
                endAt = LocalDateTime(2026, 8, 8, 10, 50),
                reservedCount = 1,
                capacity = 1,
                status = ClassSessionStatus.COMPLETED,
                members = demoMembers,
            ),
        )

    private val customCategories = mutableListOf("필라테스", "요가")

    override suspend fun getTemplates(): List<ClassTemplate> {
        delay(300)
        return templates.toList()
    }

    override suspend fun getTemplate(id: String): ClassTemplate? {
        delay(300)
        return templates.firstOrNull { it.id == id }
    }

    override suspend fun getSessions(): List<ClassSession> {
        delay(300)
        return sessions.toList()
    }

    override suspend fun getCustomCategories(): List<String> {
        delay(300)
        return customCategories.toList()
    }

    override suspend fun createTemplate(template: ClassTemplate): ClassTemplate {
        delay(300)
        val created = template.copy(id = "template-${Clock.System.now().toEpochMilliseconds()}")
        templates += created
        return created
    }

    override suspend fun updateTemplate(template: ClassTemplate): ClassTemplate {
        delay(300)
        val index = templates.indexOfFirst { it.id == template.id }
        if (index >= 0) {
            templates[index] = template
        }
        return template
    }

    override suspend fun deleteTemplate(id: String) {
        delay(300)
        templates.removeAll { it.id == id }
    }

    override suspend fun createSession(session: ClassSession): ClassSession {
        delay(300)
        val created = session.copy(id = "session-${Clock.System.now().toEpochMilliseconds()}")
        sessions += created
        return created
    }

    override suspend fun updateSession(session: ClassSession): ClassSession {
        delay(300)
        val index = sessions.indexOfFirst { it.id == session.id }
        require(index >= 0) { "수업을 찾을 수 없습니다." }
        sessions[index] = session
        return session
    }

    override suspend fun deleteSession(id: String) {
        delay(300)
        sessions.removeAll { it.id == id }
    }

    override suspend fun updateSessionMembers(
        sessionId: String,
        members: List<ClassSessionMember>,
    ) {
        delay(300)
        val index = sessions.indexOfFirst { it.id == sessionId }
        require(index >= 0) { "수업을 찾을 수 없습니다." }
        sessions[index] =
            sessions[index].copy(
                members = members,
                reservedCount = members.size,
            )
    }
}

private val demoMembers =
    listOf(
        ClassSessionMember(id = "member-1", name = "김민지"),
        ClassSessionMember(id = "member-2", name = "이서윤"),
        ClassSessionMember(id = "member-3", name = "박지수"),
    )
