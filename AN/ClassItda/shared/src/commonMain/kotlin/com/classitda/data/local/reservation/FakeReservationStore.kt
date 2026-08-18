package com.classitda.data.local.reservation

import com.classitda.domain.model.reservation.ReservationClass
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate

internal class FakeReservationStore {
    private val classes =
        mutableListOf(
            ReservationClass(
                id = "1",
                date = LocalDate(2026, 8, 8),
                classTime = "오전 10:00 - 10:50",
                className = "리포머 베이직",
                instructorName = "이지은 강사",
                roomName = "리포머룸",
                leftStudentCount = 4,
            ),
            ReservationClass(
                id = "2",
                date = LocalDate(2026, 8, 8),
                classTime = "오후 2:00 - 2:50",
                className = "체어 밸런스",
                instructorName = "박소연 강사",
                roomName = "스튜디오 A",
                leftStudentCount = 0,
            ),
            ReservationClass(
                id = "3",
                date = LocalDate(2026, 8, 7),
                classTime = "오후 7:30 - 8:20",
                className = "리포머 밸런스",
                instructorName = "이지은 강사",
                roomName = "스튜디오 B",
                leftStudentCount = 0,
                isReserved = true,
            ),
            ReservationClass(
                id = "4",
                date = LocalDate(2026, 8, 9),
                classTime = "오후 9:30 - 10:20",
                className = "체어 베이직",
                instructorName = "박소연 강사",
                roomName = "바렐룸",
                leftStudentCount = 0,
                isWaitlisted = true,
            ),
            ReservationClass(
                id = "5",
                date = LocalDate(2026, 8, 18),
                classTime = "오전 10:00 - 10:50",
                className = "리포머 베이직",
                instructorName = "이지은 강사",
                roomName = "준비물 - 수건",
                leftStudentCount = 4,
            ),
            ReservationClass(
                id = "6",
                date = LocalDate(2026, 8, 18),
                classTime = "오후 7:30 - 8:20",
                className = "체어 밸런스",
                instructorName = "박소연 강사",
                roomName = "코어 집중 수업",
                leftStudentCount = 0,
            ),
            ReservationClass(
                id = "7",
                date = LocalDate(2026, 8, 20),
                classTime = "오후 2:00 - 2:50",
                className = "리포머 밸런스",
                instructorName = "이지은 강사",
                roomName = "초급자 참여 가능",
                leftStudentCount = 0,
                isReserved = true,
            ),
            ReservationClass(
                id = "8",
                date = LocalDate(2026, 8, 22),
                classTime = "오전 11:00 - 11:50",
                className = "바렐 스트레칭",
                instructorName = "김서연 강사",
                roomName = "준비물 - 수건",
                leftStudentCount = 0,
                isWaitlisted = true,
            ),
            ReservationClass(
                id = "9",
                date = LocalDate(2026, 8, 25),
                classTime = "오전 9:00 - 9:50",
                className = "모닝 요가",
                instructorName = "최유진 강사",
                roomName = "개인 매트 지참 가능",
                leftStudentCount = 6,
            ),
            ReservationClass(
                id = "10",
                date = LocalDate(2026, 8, 25),
                classTime = "오후 6:30 - 7:20",
                className = "리포머 인터미디어트",
                instructorName = "이지은 강사",
                roomName = "중급자 대상",
                leftStudentCount = 2,
            ),
            ReservationClass(
                id = "11",
                date = LocalDate(2026, 8, 28),
                classTime = "오후 8:00 - 8:50",
                className = "체어 베이직",
                instructorName = "박소연 강사",
                roomName = null,
                leftStudentCount = 0,
                isReserved = true,
            ),
            ReservationClass(
                id = "12",
                date = LocalDate(2026, 8, 30),
                classTime = "오후 3:00 - 3:50",
                className = "릴렉스 요가",
                instructorName = "최유진 강사",
                roomName = "초급자 추천",
                leftStudentCount = 0,
                isWaitlisted = true,
            ),
            ReservationClass(
                id = "13",
                date = LocalDate(2026, 9, 2),
                classTime = "오전 10:00 - 10:50",
                className = "리포머 베이직",
                instructorName = "이지은 강사",
                roomName = "준비물 - 수건",
                leftStudentCount = 5,
            ),
            ReservationClass(
                id = "14",
                date = LocalDate(2026, 9, 2),
                classTime = "오후 7:30 - 8:20",
                className = "체어 밸런스",
                instructorName = "박소연 강사",
                roomName = "코어 집중 수업",
                leftStudentCount = 0,
            ),
            ReservationClass(
                id = "15",
                date = LocalDate(2026, 9, 5),
                classTime = "오전 11:00 - 11:50",
                className = "바렐 스트레칭",
                instructorName = "김서연 강사",
                roomName = "초급자 참여 가능",
                leftStudentCount = 0,
                isReserved = true,
            ),
            ReservationClass(
                id = "16",
                date = LocalDate(2026, 9, 9),
                classTime = "오후 6:30 - 7:20",
                className = "리포머 인터미디어트",
                instructorName = "이지은 강사",
                roomName = "중급자 대상",
                leftStudentCount = 0,
                isWaitlisted = true,
            ),
            ReservationClass(
                id = "17",
                date = LocalDate(2026, 9, 12),
                classTime = "오전 9:00 - 9:50",
                className = "모닝 요가",
                instructorName = "최유진 강사",
                roomName = "개인 매트 지참 가능",
                leftStudentCount = 7,
            ),
            ReservationClass(
                id = "18",
                date = LocalDate(2026, 9, 12),
                classTime = "오후 2:00 - 2:50",
                className = "리포머 밸런스",
                instructorName = "이지은 강사",
                roomName = null,
                leftStudentCount = 3,
            ),
            ReservationClass(
                id = "19",
                date = LocalDate(2026, 9, 16),
                classTime = "오후 8:00 - 8:50",
                className = "체어 베이직",
                instructorName = "박소연 강사",
                roomName = "준비물 - 수건",
                leftStudentCount = 0,
                isReserved = true,
            ),
            ReservationClass(
                id = "20",
                date = LocalDate(2026, 9, 20),
                classTime = "오후 3:00 - 3:50",
                className = "릴렉스 요가",
                instructorName = "최유진 강사",
                roomName = "초급자 추천",
                leftStudentCount = 0,
                isWaitlisted = true,
            ),
            ReservationClass(
                id = "21",
                date = LocalDate(2026, 9, 24),
                classTime = "오전 10:00 - 10:50",
                className = "리포머 베이직",
                instructorName = "이지은 강사",
                roomName = "준비물 - 수건",
                leftStudentCount = 4,
            ),
            ReservationClass(
                id = "22",
                date = LocalDate(2026, 9, 24),
                classTime = "오후 7:30 - 8:20",
                className = "체어 밸런스",
                instructorName = "박소연 강사",
                roomName = "코어 집중 수업",
                leftStudentCount = 0,
            ),
            ReservationClass(
                id = "23",
                date = LocalDate(2026, 9, 28),
                classTime = "오후 6:30 - 7:20",
                className = "리포머 인터미디어트",
                instructorName = "이지은 강사",
                roomName = "중급자 대상",
                leftStudentCount = 0,
                isReserved = true,
            ),
            ReservationClass(
                id = "24",
                date = LocalDate(2026, 9, 30),
                classTime = "오후 8:00 - 8:50",
                className = "바렐 스트레칭",
                instructorName = "김서연 강사",
                roomName = null,
                leftStudentCount = 0,
                isWaitlisted = true,
            ),
        ).apply {
            addAll(createWeekdayClasses(existingDates = map { it.date }.toSet()))
        }

    fun getClasses(): List<ReservationClass> = classes.toList()

    fun getClassById(classId: String): ReservationClass? = classes.firstOrNull { it.id == classId }

    fun saveReservation(classId: String) {
        updateClass(classId) { it.copy(isReserved = true, isWaitlisted = false) }
    }

    fun saveWaitlist(classId: String) {
        updateClass(classId) { it.copy(isReserved = false, isWaitlisted = true) }
    }

    private fun updateClass(
        classId: String,
        transform: (ReservationClass) -> ReservationClass,
    ) {
        val index = classes.indexOfFirst { it.id == classId }
        if (index >= 0) classes[index] = transform(classes[index])
    }
}

private fun createWeekdayClasses(existingDates: Set<LocalDate>): List<ReservationClass> =
    buildList {
        for (month in 8..9) {
            val lastDayOfMonth = if (month == 8) 31 else 30
            for (dayOfMonth in 1..lastDayOfMonth) {
                val date = LocalDate(2026, month, dayOfMonth)
                val isWeekday =
                    date.dayOfWeek != DayOfWeek.SATURDAY &&
                        date.dayOfWeek != DayOfWeek.SUNDAY
                if (!isWeekday || date in existingDates) continue

                val variant = (month + dayOfMonth) % 3
                add(
                    ReservationClass(
                        id = "weekday-$month-$dayOfMonth",
                        date = date,
                        classTime =
                            when (variant) {
                                0 -> "오전 10:00 - 10:50"
                                1 -> "오후 2:00 - 2:50"
                                else -> "오후 7:30 - 8:20"
                            },
                        className =
                            when (variant) {
                                0 -> "리포머 베이직"
                                1 -> "체어 밸런스"
                                else -> "릴렉스 요가"
                            },
                        instructorName =
                            when (variant) {
                                0 -> "이지은 강사"
                                1 -> "박소연 강사"
                                else -> "최유진 강사"
                            },
                        roomName =
                            when (variant) {
                                0 -> "준비물 - 수건"
                                1 -> "코어 집중 수업"
                                else -> "개인 매트 지참 가능"
                            },
                        leftStudentCount = 2 + dayOfMonth % 5,
                    ),
                )
            }
        }
    }

internal fun LocalDate.toReservationDateText(): String =
    "${toString().replace('-', '.')} (${dayOfWeek.koreanShortName})"

private val DayOfWeek.koreanShortName: String
    get() =
        when (this) {
            DayOfWeek.MONDAY -> "월"
            DayOfWeek.TUESDAY -> "화"
            DayOfWeek.WEDNESDAY -> "수"
            DayOfWeek.THURSDAY -> "목"
            DayOfWeek.FRIDAY -> "금"
            DayOfWeek.SATURDAY -> "토"
            DayOfWeek.SUNDAY -> "일"
        }
