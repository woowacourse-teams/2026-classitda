package com.classitda.feature.instructor.schedule

import com.classitda.core.studio.InstructorStudioContext
import com.classitda.domain.model.instructor.session.InstructorCalendarDay
import com.classitda.domain.model.instructor.session.InstructorClassForm
import com.classitda.domain.model.instructor.session.InstructorClassType
import com.classitda.domain.model.instructor.session.InstructorDailySession
import com.classitda.domain.model.instructor.session.InstructorSessionDetail
import com.classitda.domain.model.instructor.session.InstructorSessionStatus
import com.classitda.domain.model.instructor.session.InstructorSessionUpdate
import com.classitda.domain.model.studio.Studio
import com.classitda.domain.model.studio.StudioId
import com.classitda.domain.repository.instructor.session.InstructorSessionRepository
import com.classitda.domain.repository.studio.StudioRepository
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

@OptIn(ExperimentalCoroutinesApi::class)
class InstructorScheduleViewModelTest {
    @BeforeTest
    fun setUpMainDispatcher() {
        Dispatchers.setMain(Dispatchers.Unconfined)
    }

    @AfterTest
    fun resetMainDispatcher() {
        Dispatchers.resetMain()
    }

    @Test
    fun `날짜를 변경하면 캘린더를 유지하고 수업 목록만 다시 불러온다`() =
        runBlocking {
            val firstDate = LocalDate(2026, 8, 5)
            val nextDate = LocalDate(2026, 8, 6)
            val nextSessions = CompletableDeferred<List<InstructorDailySession>>()
            val calendarDays = listOf(InstructorCalendarDay(firstDate, true, false, true, false))
            val repository = RecordingSessionRepository(nextDate, nextSessions, calendarDays)
            val viewModel =
                InstructorScheduleViewModel(
                    repository = repository,
                    studioContext = InstructorStudioContext(FixedStudioRepository),
                )

            viewModel.load(firstDate)

            val initial = assertIs<InstructorScheduleUiState.Success>(viewModel.uiState.value)
            val initialSessions = assertIs<InstructorScheduleListUiState.Content>(initial.sessionList)
            assertEquals(listOf("session-5"), initialSessions.sessions.map { it.id })
            assertEquals(calendarDays, initial.calendarDays)

            viewModel.load(nextDate)

            val loading = assertIs<InstructorScheduleUiState.Success>(viewModel.uiState.value)
            assertIs<InstructorScheduleListUiState.Loading>(loading.sessionList)
            assertEquals(calendarDays, loading.calendarDays)
            assertEquals(1, repository.calendarRequestCount)

            nextSessions.complete(listOf(session(nextDate, "session-6")))

            val loaded = assertIs<InstructorScheduleUiState.Success>(viewModel.uiState.value)
            val loadedSessions = assertIs<InstructorScheduleListUiState.Content>(loaded.sessionList)
            assertEquals(listOf("session-6"), loadedSessions.sessions.map { it.id })
            assertEquals(calendarDays, loaded.calendarDays)
            assertEquals(listOf(firstDate, nextDate), repository.dailyRequests)
            assertEquals(1, repository.calendarRequestCount)
        }
}

private class RecordingSessionRepository(
    private val deferredDate: LocalDate,
    private val deferredSessions: CompletableDeferred<List<InstructorDailySession>>,
    private val calendarDays: List<InstructorCalendarDay>,
) : InstructorSessionRepository {
    val dailyRequests = mutableListOf<LocalDate>()
    var calendarRequestCount: Int = 0
        private set

    override suspend fun getDailySessions(
        studioId: StudioId,
        date: LocalDate,
    ): List<InstructorDailySession> {
        dailyRequests += date
        return if (date == deferredDate) deferredSessions.await() else listOf(session(date, "session-${date.day}"))
    }

    override suspend fun getCalendar(
        studioId: StudioId,
        from: LocalDate,
        to: LocalDate,
    ): List<InstructorCalendarDay> {
        calendarRequestCount += 1
        return calendarDays
    }

    override suspend fun getSession(
        studioId: StudioId,
        sessionId: String,
    ): InstructorSessionDetail = error("사용하지 않는 테스트 경로입니다.")

    override suspend fun updateSession(
        studioId: StudioId,
        sessionId: String,
        update: InstructorSessionUpdate,
    ) = error("사용하지 않는 테스트 경로입니다.")

    override suspend fun cancelSession(
        studioId: StudioId,
        sessionId: String,
    ) = error("사용하지 않는 테스트 경로입니다.")
}

private object FixedStudioRepository : StudioRepository {
    override suspend fun getMyStudios(): List<Studio> =
        listOf(
            Studio(
                id = StudioId("studio-1"),
                name = "잇다 스튜디오",
                address = "서울",
                phoneNumber = "0212345678",
                openTime = null,
                closeTime = null,
                imageUrl = null,
                description = null,
            ),
        )
}

private fun session(
    date: LocalDate,
    id: String,
) = InstructorDailySession(
    id = id,
    studioId = StudioId("studio-1"),
    instructorMembershipId = "membership-1",
    instructorName = "김강사",
    classForm = InstructorClassForm.GROUP,
    classType = InstructorClassType("type-1", "그룹 수업"),
    className = "리포머 밸런스",
    description = null,
    capacity = 8,
    reservedCount = 4,
    waitingCount = 0,
    startAt = LocalDateTime(date.year, date.month, date.day, 19, 0),
    endAt = LocalDateTime(date.year, date.month, date.day, 19, 50),
    status = InstructorSessionStatus.SCHEDULED_BOOKING_OPEN,
    mine = true,
)
