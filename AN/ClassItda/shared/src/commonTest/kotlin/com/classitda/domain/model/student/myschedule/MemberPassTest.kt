package com.classitda.domain.model.student.myschedule

import kotlinx.datetime.LocalDate
import kotlin.test.Test
import kotlin.test.assertFailsWith

class MemberPassTest {
    @Test
    fun `수강권 이름이 blank이면 생성할 수 없다`() {
        assertFailsWith<IllegalArgumentException> {
            createPass(name = " \t")
        }
    }

    @Test
    fun `수강권 유효기간의 시작일과 종료일이 같으면 생성할 수 있다`() {
        val date = LocalDate(2026, 8, 17)

        createPass(validFrom = date, validUntil = date)
    }

    @Test
    fun `수강권 유효기간의 시작일이 종료일보다 늦으면 생성할 수 없다`() {
        assertFailsWith<IllegalArgumentException> {
            createPass(
                validFrom = LocalDate(2026, 8, 18),
                validUntil = LocalDate(2026, 8, 17),
            )
        }
    }

    @Test
    fun `수강권 횟수가 모두 0이면 생성할 수 있다`() {
        createAvailability(
            remainingUses = 0,
            reservableUses = 0,
            cancellableUses = 0,
        )
    }

    @Test
    fun `잔여 횟수가 음수이면 생성할 수 없다`() {
        assertFailsWith<IllegalArgumentException> {
            createAvailability(remainingUses = -1)
        }
    }

    @Test
    fun `예약 가능 횟수가 음수이면 생성할 수 없다`() {
        assertFailsWith<IllegalArgumentException> {
            createAvailability(reservableUses = -1)
        }
    }

    @Test
    fun `취소 가능 횟수가 음수이면 생성할 수 없다`() {
        assertFailsWith<IllegalArgumentException> {
            createAvailability(cancellableUses = -1)
        }
    }

    @Test
    fun `수강권 횟수 사이의 대소 관계는 제한하지 않는다`() {
        createAvailability(
            remainingUses = 1,
            reservableUses = 3,
            cancellableUses = 5,
        )
        createAvailability(
            remainingUses = 5,
            reservableUses = 3,
            cancellableUses = 1,
        )
    }

    private fun createPass(
        name: String = "필라테스 10회권",
        validFrom: LocalDate = LocalDate(2026, 8, 1),
        validUntil: LocalDate = LocalDate(2026, 9, 30),
    ): MemberPassSummary =
        MemberPassSummary(
            id = MemberPassId("member-pass-1"),
            name = name,
            validFrom = validFrom,
            validUntil = validUntil,
        )

    private fun createAvailability(
        remainingUses: Int = 3,
        reservableUses: Int = 2,
        cancellableUses: Int = 1,
    ): MemberPassAvailability =
        MemberPassAvailability(
            pass = createPass(),
            remainingUses = remainingUses,
            reservableUses = reservableUses,
            cancellableUses = cancellableUses,
        )
}
