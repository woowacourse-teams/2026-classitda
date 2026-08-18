package com.classitda.domain.model.student.myschedule

import kotlin.test.Test
import kotlin.test.assertFailsWith
import kotlin.time.Instant

class ClassSessionTest {
    @Test
    fun `수업 시작과 종료 시각이 같으면 생성할 수 없다`() {
        val instant = Instant.parse("2026-08-17T01:00:00Z")

        assertFailsWith<IllegalArgumentException> {
            createPeriod(startsAt = instant, endsAt = instant)
        }
    }

    @Test
    fun `수업 시작 시각이 종료 시각보다 늦으면 생성할 수 없다`() {
        assertFailsWith<IllegalArgumentException> {
            createPeriod(
                startsAt = Instant.parse("2026-08-17T02:00:00Z"),
                endsAt = Instant.parse("2026-08-17T01:00:00Z"),
            )
        }
    }

    @Test
    fun `유효하지 않은 시간대 식별자이면 생성할 수 없다`() {
        assertFailsWith<IllegalArgumentException> {
            createPeriod(timeZoneId = "Not/A_Real_Time_Zone")
        }
    }

    @Test
    fun `시간대 식별자가 blank이면 생성할 수 없다`() {
        assertFailsWith<IllegalArgumentException> {
            createPeriod(timeZoneId = " \t")
        }
    }

    @Test
    fun `수업 제목이 blank이면 생성할 수 없다`() {
        assertFailsWith<IllegalArgumentException> {
            createSession(title = " \n")
        }
    }

    @Test
    fun `강사 이름이 blank이면 생성할 수 없다`() {
        assertFailsWith<IllegalArgumentException> {
            createInstructor(name = " \t")
        }
    }

    @Test
    fun `시설 이름이 blank이면 생성할 수 없다`() {
        assertFailsWith<IllegalArgumentException> {
            createFacility(name = "\n")
        }
    }

    @Test
    fun `메모가 blank이면 생성할 수 없다`() {
        assertFailsWith<IllegalArgumentException> {
            createSession(memo = " \t\n")
        }
    }

    @Test
    fun `메모가 null이면 생성할 수 있다`() {
        createSession(memo = null)
    }

    private fun createSession(
        title: String = "리포머 베이직",
        memo: String? = "10분 전까지 도착",
    ): ClassSession =
        ClassSession(
            id = ClassSessionId("session-1"),
            title = title,
            period = createPeriod(),
            instructor = createInstructor(),
            facility = createFacility(),
            memo = memo,
        )

    private fun createPeriod(
        startsAt: Instant = Instant.parse("2026-08-17T01:00:00Z"),
        endsAt: Instant = Instant.parse("2026-08-17T02:00:00Z"),
        timeZoneId: String = "Asia/Seoul",
    ): ClassPeriod =
        ClassPeriod(
            startsAt = startsAt,
            endsAt = endsAt,
            timeZoneId = timeZoneId,
        )

    private fun createInstructor(name: String = "홍길동"): InstructorSummary =
        InstructorSummary(
            id = InstructorId("instructor-1"),
            name = name,
            profileImageUrl = null,
        )

    private fun createFacility(name: String = "강남점"): FacilitySummary =
        FacilitySummary(
            id = FacilityId("facility-1"),
            name = name,
        )
}
